package com.dma.finance.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un utilisateur de l'application. Le mot de passe n'est jamais stocké en clair :
 * seuls le hash PBKDF2 et le sel sont conservés (voir [com.dma.finance.data.security.PasswordHasher]).
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val passwordSalt: String,
    val createdAt: Long = System.currentTimeMillis()
)
