package com.dma.finance.data.remote

import com.dma.finance.data.local.dao.ProjectDao
import com.dma.finance.data.local.dao.ProjectMemberDao
import com.dma.finance.data.local.dao.UserDao
import com.dma.finance.data.local.entity.ProjectEntity
import com.dma.finance.data.local.entity.ProjectMemberEntity
import com.dma.finance.data.local.entity.ProjectRole
import com.dma.finance.data.local.entity.UserEntity
import com.dma.finance.data.security.PasswordHasher
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Synchronise les projets et leurs membres avec Firestore, pour qu'un projet partagé
 * apparaisse automatiquement sur tous les appareils des membres invités. Les comptes,
 * catégories, transactions et budgets ne sont pas encore synchronisés (étape suivante) :
 * seule la collaboration (qui appartient à quel projet, avec quel rôle) l'est ici.
 *
 * Un utilisateur distant jamais connecté sur cet appareil est représenté localement par
 * une entrée "fantôme" dans la table users (mot de passe aléatoire inutilisable), pour
 * respecter les contraintes de clé étrangère existantes sans modifier le schéma local.
 */
@Singleton
class FirestoreProjectSync @Inject constructor(
    private val projectDao: ProjectDao,
    private val projectMemberDao: ProjectMemberDao,
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher,
    private val firebaseAuthBridge: FirebaseAuthBridge
) {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var listenerRegistration: ListenerRegistration? = null

    /** Pousse l'état complet local (projet + membres) vers Firestore. Best-effort, ne lève jamais. */
    suspend fun syncProjectToCloud(projectId: Long) {
        try {
            val project = projectDao.findById(projectId) ?: return
            val owner = userDao.findById(project.ownerId) ?: return
            val members = projectMemberDao.findAllForProjectOnce(projectId)

            val resolvedMembers = members.map { member ->
                if (member.memberFirebaseUid != null) {
                    member
                } else {
                    val user = userDao.findById(member.userId)
                    val uid = user?.let { firebaseAuthBridge.resolveUidForEmail(it.email) }
                    if (uid != null) {
                        val updated = member.copy(memberFirebaseUid = uid)
                        projectMemberDao.update(updated)
                        updated
                    } else {
                        member
                    }
                }
            }

            val memberUids = resolvedMembers.mapNotNull { it.memberFirebaseUid }.distinct()

            firestore.collection("projects").document(project.firebaseId)
                .set(
                    mapOf(
                        "name" to project.name,
                        "description" to project.description,
                        "currencyCode" to project.currencyCode,
                        "ownerEmail" to owner.email,
                        "ownerFullName" to owner.fullName,
                        "memberFirebaseUids" to memberUids,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            val membersCollection = firestore.collection("projects").document(project.firebaseId).collection("members")
            for (member in resolvedMembers) {
                val user = userDao.findById(member.userId) ?: continue
                membersCollection.document(member.firebaseId)
                    .set(
                        mapOf(
                            "email" to user.email,
                            "fullName" to user.fullName,
                            "role" to member.role.name,
                            "firebaseUid" to member.memberFirebaseUid
                        )
                    )
                    .await()
            }

            val localFirebaseIds = resolvedMembers.map { it.firebaseId }.toSet()
            val remoteMembers = membersCollection.get().await()
            for (doc in remoteMembers.documents) {
                if (doc.id !in localFirebaseIds) {
                    membersCollection.document(doc.id).delete().await()
                }
            }
        } catch (e: Exception) {
            // Best-effort : la synchronisation réessaiera à la prochaine modification locale.
        }
    }

    /** Écoute en temps réel les projets Firestore dont l'utilisateur courant est membre. */
    fun startListening() {
        val uid = firebaseAuthBridge.currentFirebaseUid ?: return
        stopListening()
        listenerRegistration = firestore.collection("projects")
            .whereArrayContains("memberFirebaseUids", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                for (change in snapshot.documentChanges) {
                    val doc = change.document
                    scope.launch { applyRemoteProject(doc.id, doc.data) }
                }
            }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    /** Rétablit l'UID Firebase des adhésions en attente pour cet e-mail, puis repousse leurs projets. */
    suspend fun reconcileUnresolvedMemberships(email: String, uid: String) {
        try {
            val pending = projectMemberDao.findAllWithoutFirebaseUid()
            for (member in pending) {
                val user = userDao.findById(member.userId) ?: continue
                if (!user.email.equals(email, ignoreCase = true)) continue
                projectMemberDao.update(member.copy(memberFirebaseUid = uid))
                syncProjectToCloud(member.projectId)
            }
        } catch (e: Exception) {
            // Best-effort.
        }
    }

    private suspend fun applyRemoteProject(firebaseId: String, data: Map<String, Any?>?) {
        if (data == null) return
        try {
            val name = data["name"] as? String ?: return
            val description = data["description"] as? String ?: ""
            val currencyCode = data["currencyCode"] as? String ?: "XOF"
            val ownerEmail = data["ownerEmail"] as? String ?: return
            val ownerFullName = data["ownerFullName"] as? String ?: ownerEmail

            val ownerId = resolveOrCreateLocalUser(ownerEmail, ownerFullName)
            val existing = projectDao.findByFirebaseId(firebaseId)
            val localProjectId = if (existing != null) {
                projectDao.update(
                    existing.copy(name = name, description = description, currencyCode = currencyCode, ownerId = ownerId)
                )
                existing.id
            } else {
                projectDao.insert(
                    ProjectEntity(
                        name = name,
                        description = description,
                        currencyCode = currencyCode,
                        ownerId = ownerId,
                        firebaseId = firebaseId
                    )
                )
            }

            val remoteMembers = firestore.collection("projects").document(firebaseId)
                .collection("members").get().await()

            val remoteFirebaseIds = mutableSetOf<String>()
            for (doc in remoteMembers.documents) {
                val email = doc.getString("email") ?: continue
                val fullName = doc.getString("fullName") ?: email
                val roleName = doc.getString("role") ?: continue
                val role = try {
                    ProjectRole.valueOf(roleName)
                } catch (e: IllegalArgumentException) {
                    continue
                }
                val firebaseUid = doc.getString("firebaseUid")
                remoteFirebaseIds.add(doc.id)

                val memberUserId = resolveOrCreateLocalUser(email, fullName)
                val localMember = projectMemberDao.findByFirebaseId(localProjectId, doc.id)
                if (localMember != null) {
                    if (localMember.role != role || localMember.memberFirebaseUid != firebaseUid) {
                        projectMemberDao.update(localMember.copy(role = role, memberFirebaseUid = firebaseUid))
                    }
                } else {
                    projectMemberDao.insert(
                        ProjectMemberEntity(
                            projectId = localProjectId,
                            userId = memberUserId,
                            role = role,
                            firebaseId = doc.id,
                            memberFirebaseUid = firebaseUid
                        )
                    )
                }
            }

            val localMembers = projectMemberDao.findAllForProjectOnce(localProjectId)
            for (localMember in localMembers) {
                if (localMember.firebaseId !in remoteFirebaseIds) {
                    projectMemberDao.delete(localMember)
                }
            }
        } catch (e: Exception) {
            // Best-effort : la prochaine mise à jour distante corrigera l'état local.
        }
    }

    private suspend fun resolveOrCreateLocalUser(email: String, fullName: String): Long {
        val trimmedEmail = email.trim()
        userDao.findByEmail(trimmedEmail)?.let { return it.id }
        val salt = passwordHasher.generateSalt()
        val randomPassword = UUID.randomUUID().toString()
        val hash = passwordHasher.hash(randomPassword, salt)
        return userDao.insert(
            UserEntity(fullName = fullName, email = trimmedEmail, passwordHash = hash, passwordSalt = salt)
        )
    }
}
