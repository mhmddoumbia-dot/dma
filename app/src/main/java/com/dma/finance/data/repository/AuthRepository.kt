package com.dma.finance.data.repository

import com.dma.finance.data.local.dao.UserDao
import com.dma.finance.data.local.entity.UserEntity
import com.dma.finance.data.security.PasswordHasher
import com.dma.finance.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/** Gère l'inscription, la connexion et la session de l'utilisateur courant. */
@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher,
    private val sessionManager: SessionManager
) {
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
        return AuthResult.Success(user.copy(id = id))
    }

    suspend fun login(email: String, password: String): AuthResult {
        val user = userDao.findByEmail(email.trim())
            ?: return AuthResult.Error("INVALID_CREDENTIALS")
        val valid = passwordHasher.verify(password, user.passwordSalt, user.passwordHash)
        if (!valid) return AuthResult.Error("INVALID_CREDENTIALS")
        sessionManager.setCurrentUser(user.id)
        return AuthResult.Success(user)
    }

    suspend fun signOut() {
        sessionManager.signOut()
    }
}
