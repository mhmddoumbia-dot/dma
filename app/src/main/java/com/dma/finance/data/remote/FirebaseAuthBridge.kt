package com.dma.finance.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pont vers Firebase Authentication, utilisé en parallèle de l'authentification locale
 * (Room + PBKDF2) pour donner à chaque utilisateur une identité stable côté cloud,
 * nécessaire à la synchronisation Firestore. L'authentification locale reste la seule
 * source de vérité pour l'accès à l'application : toute erreur ici (pas de réseau,
 * projet Firebase non configuré...) est absorbée silencieusement et n'affecte jamais
 * la connexion locale.
 *
 * Tient également à jour un annuaire public `usersByEmail/{email} -> uid` dans Firestore,
 * nécessaire pour qu'un utilisateur puisse en inviter un autre par e-mail sur un projet
 * partagé avant même que celui-ci ait rejoint l'appareil courant.
 */
@Singleton
class FirebaseAuthBridge @Inject constructor() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val currentFirebaseUid: String? get() = auth.currentUser?.uid

    /**
     * Assure qu'un compte Firebase existe pour cet e-mail/mot de passe et que
     * l'utilisateur y est connecté : tente une connexion, puis crée le compte
     * si nécessaire. Ne lève jamais d'exception.
     */
    suspend fun ensureSignedIn(email: String, password: String): String? {
        val uid = try {
            auth.signInWithEmailAndPassword(email, password).await().user?.uid
        } catch (signInError: Exception) {
            try {
                auth.createUserWithEmailAndPassword(email, password).await().user?.uid
            } catch (createError: Exception) {
                null
            }
        }
        if (uid != null) {
            registerEmailDirectory(email, uid)
        }
        return uid
    }

    /** Résout l'UID Firebase associé à un e-mail via l'annuaire public, ou null si inconnu/hors-ligne. */
    suspend fun resolveUidForEmail(email: String): String? {
        return try {
            firestore.collection("usersByEmail")
                .document(email.trim().lowercase())
                .get().await()
                .getString("uid")
        } catch (e: Exception) {
            null
        }
    }

    private fun registerEmailDirectory(email: String, uid: String) {
        try {
            firestore.collection("usersByEmail")
                .document(email.trim().lowercase())
                .set(mapOf("uid" to uid))
        } catch (e: Exception) {
            // Ignoré : ce n'est qu'un annuaire best-effort.
        }
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Ignoré : la déconnexion locale prime.
        }
    }
}
