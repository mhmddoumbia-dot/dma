package com.dma.finance.data.remote

import com.google.firebase.auth.FirebaseAuth
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
 */
@Singleton
class FirebaseAuthBridge @Inject constructor() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val currentFirebaseUid: String? get() = auth.currentUser?.uid

    /**
     * Assure qu'un compte Firebase existe pour cet e-mail/mot de passe et que
     * l'utilisateur y est connecté : tente une connexion, puis crée le compte
     * si nécessaire. Ne lève jamais d'exception.
     */
    suspend fun ensureSignedIn(email: String, password: String): String? {
        return try {
            auth.signInWithEmailAndPassword(email, password).await().user?.uid
        } catch (signInError: Exception) {
            try {
                auth.createUserWithEmailAndPassword(email, password).await().user?.uid
            } catch (createError: Exception) {
                null
            }
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
