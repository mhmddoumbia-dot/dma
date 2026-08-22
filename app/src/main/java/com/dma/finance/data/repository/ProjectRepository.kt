package com.dma.finance.data.repository

import com.dma.finance.data.local.dao.AccountDao
import com.dma.finance.data.local.dao.CategoryDao
import com.dma.finance.data.local.dao.ProjectDao
import com.dma.finance.data.local.dao.ProjectMemberDao
import com.dma.finance.data.local.dao.UserDao
import com.dma.finance.data.local.entity.AccountEntity
import com.dma.finance.data.local.entity.AccountType
import com.dma.finance.data.local.entity.ProjectEntity
import com.dma.finance.data.local.entity.ProjectMemberEntity
import com.dma.finance.data.local.entity.ProjectRole
import com.dma.finance.data.local.entity.UserEntity
import com.dma.finance.data.local.relation.ProjectMemberWithUser
import com.dma.finance.data.remote.FirestoreProjectSync
import com.dma.finance.data.security.PasswordHasher
import com.dma.finance.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class ProjectMemberResult {
    object Success : ProjectMemberResult()
    object UserNotFound : ProjectMemberResult()
    object AlreadyMember : ProjectMemberResult()
    object LastOwner : ProjectMemberResult()
}

/**
 * Gère les projets financiers et leurs membres. C'est le cœur de la fonctionnalité
 * multi-utilisateurs : un projet peut être partagé avec d'autres personnes par e-mail,
 * même si elles n'ont jamais utilisé l'application sur cet appareil (une identité locale
 * "fantôme" est créée pour elles, complétée automatiquement quand Firestore synchronise
 * leur vrai nom depuis leur propre appareil). Chaque membre a un rôle ([ProjectRole]) qui
 * borne ses permissions. Toute modification est répercutée vers Firestore en arrière-plan
 * (best-effort) pour que le projet apparaisse sur les autres appareils des membres.
 */
@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val projectMemberDao: ProjectMemberDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher,
    private val sessionManager: SessionManager,
    private val firestoreProjectSync: FirestoreProjectSync
) {
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val currentProjectId: Flow<Long?> = sessionManager.currentProjectId

    fun observeProjectsForUser(userId: Long): Flow<List<ProjectEntity>> =
        projectDao.observeProjectsForUser(userId)

    fun observeProject(projectId: Long): Flow<ProjectEntity?> = projectDao.observeById(projectId)

    fun observeMembers(projectId: Long): Flow<List<ProjectMemberWithUser>> =
        projectMemberDao.observeMembersForProject(projectId)

    fun observeMyRole(projectId: Long, userId: Long): Flow<ProjectMemberEntity?> =
        projectMemberDao.observeMember(projectId, userId)

    suspend fun createProject(
        name: String,
        description: String,
        currencyCode: String,
        ownerUserId: Long
    ): Long {
        val project = ProjectEntity(
            name = name.trim(),
            description = description.trim(),
            currencyCode = currencyCode,
            ownerId = ownerUserId
        )
        val projectId = projectDao.insert(project)
        projectMemberDao.insert(
            ProjectMemberEntity(projectId = projectId, userId = ownerUserId, role = ProjectRole.OWNER)
        )
        DefaultCategories.forProject(projectId).forEach { categoryDao.insert(it) }
        accountDao.insert(AccountEntity(projectId = projectId, name = "Espèces", type = AccountType.CASH))
        sessionManager.setCurrentProject(projectId)
        syncToCloud(projectId)
        return projectId
    }

    suspend fun selectProject(projectId: Long) {
        sessionManager.setCurrentProject(projectId)
    }

    /**
     * Ajoute un membre par e-mail. Si personne n'utilise encore cette adresse sur cet
     * appareil, une identité locale minimale est créée : son vrai nom sera complété
     * automatiquement dès que la synchronisation Firestore aura connaissance de son
     * propre compte (créé sur son propre appareil).
     */
    suspend fun addMemberByEmail(projectId: Long, email: String, role: ProjectRole): ProjectMemberResult {
        val trimmedEmail = email.trim()
        if (!trimmedEmail.contains("@")) return ProjectMemberResult.UserNotFound
        val user = userDao.findByEmail(trimmedEmail) ?: createShadowUser(trimmedEmail)
        val existing = projectMemberDao.findMember(projectId, user.id)
        if (existing != null) return ProjectMemberResult.AlreadyMember
        projectMemberDao.insert(ProjectMemberEntity(projectId = projectId, userId = user.id, role = role))
        syncToCloud(projectId)
        return ProjectMemberResult.Success
    }

    suspend fun updateMemberRole(member: ProjectMemberEntity, newRole: ProjectRole): ProjectMemberResult {
        if (member.role == ProjectRole.OWNER && newRole != ProjectRole.OWNER) {
            if (projectMemberDao.countOwners(member.projectId) <= 1) return ProjectMemberResult.LastOwner
        }
        projectMemberDao.update(member.copy(role = newRole))
        syncToCloud(member.projectId)
        return ProjectMemberResult.Success
    }

    suspend fun removeMember(member: ProjectMemberEntity): ProjectMemberResult {
        if (member.role == ProjectRole.OWNER && projectMemberDao.countOwners(member.projectId) <= 1) {
            return ProjectMemberResult.LastOwner
        }
        projectMemberDao.delete(member)
        syncToCloud(member.projectId)
        return ProjectMemberResult.Success
    }

    suspend fun deleteProject(project: ProjectEntity) {
        projectDao.delete(project)
    }

    suspend fun updateProject(project: ProjectEntity) {
        projectDao.update(project)
        syncToCloud(project.id)
    }

    private suspend fun createShadowUser(email: String): UserEntity {
        val salt = passwordHasher.generateSalt()
        val hash = passwordHasher.hash(UUID.randomUUID().toString(), salt)
        val id = userDao.insert(UserEntity(fullName = email, email = email, passwordHash = hash, passwordSalt = salt))
        return checkNotNull(userDao.findById(id))
    }

    private fun syncToCloud(projectId: Long) {
        backgroundScope.launch { firestoreProjectSync.syncProjectToCloud(projectId) }
    }
}
