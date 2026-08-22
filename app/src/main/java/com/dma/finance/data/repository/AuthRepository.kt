package com.dma.finance.data.repository

import com.dma.finance.data.local.dao.UserDao
import com.dma.finance.data.local.entity.UserEntity
import com.dma.finance.data.remote.FirebaseAuthBridge
import com.dma.finance.data.remote.FirestoreProjectSync
import com.dma.finance.data.security.PasswordHasher
import com.dma.finance.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Gère l'inscription, la connexion et la session de l'utilisateur courant.
 * L'authentification locale (Room + PBKDF2) reste la seule source de vérité pour
 * l'accès à l'application, y compris hors-ligne. En parallèle, un compte Firebase
 * miroir est créé/connecté en arrière-plan ([FirebaseAuthBridge]) pour donner à chaque
 * utilisateur une identité cloud stable, nécessaire à la synchronisation à venir —
 * cette étape est best-effort et n'affecte jamais le flux de connexion local.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher,
    private val sessionManager: SessionManager,
    private val firebaseAuthBridge: FirebaseAuthBridge,
    private val firestoreProjectSync: FirestoreProjectSync
) {
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val currentUser: Flow<UserEntity?> = sessionManager.currentUserId.flatMapLatest { userId ->
        if (userId == null) kotlinx.coroutines.flow.flowOf(null) else userDao.observeById(userId)
    }

    suspend fun register(fullName: String, email: String, password: String): AuthResult {
        val trimmedEmail = email.trim()
        if (userDao.findByEmail(trimmedEmail) != null) {
            return AuthResult.Error("EMAIL_TAKEN")
        }
        val salt = passwordHasher.generateSalt()
        val hash = passwordHasher.hash(password, salt)
        val user = UserEntity(
            fullName = fullName.trim(),
            email = trimmedEmail,
            passwordHash = hash,
            passwordSalt = salt
        )
        val id = userDao.insert(user)
        sessionManager.setCurrentUser(id)
        backgroundScope.launch {
            val uid = firebaseAuthBridge.ensureSignedIn(trimmedEmail, password)
            if (uid != null) firestoreProjectSync.reconcileUnresolvedMemberships(trimmedEmail, uid)
        }
        return AuthResult.Success(user.copy(id = id))
    }

    suspend fun login(email: String, password: String): AuthResult {
        val trimmedEmail = email.trim()
        val user = userDao.findByEmail(trimmedEmail)
            ?: return AuthResult.Error("INVALID_CREDENTIALS")
        val valid = passwordHasher.verify(password, user.passwordSalt, user.passwordHash)
        if (!valid) return AuthResult.Error("INVALID_CREDENTIALS")
        sessionManager.setCurrentUser(user.id)
        backgroundScope.launch {
            val uid = firebaseAuthBridge.ensureSignedIn(trimmedEmail, password)
            if (uid != null) firestoreProjectSync.reconcileUnresolvedMemberships(trimmedEmail, uid)
        }
        return AuthResult.Success(user)
    }

    suspend fun signOut() {
        sessionManager.signOut()
        firebaseAuthBridge.signOut()
    }
}
