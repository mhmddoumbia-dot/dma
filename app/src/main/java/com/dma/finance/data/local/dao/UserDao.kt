package com.dma.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dma.finance.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun findById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email LIKE '%' || :query || '%' COLLATE NOCASE ORDER BY fullName LIMIT 20")
    suspend fun searchByEmail(query: String): List<UserEntity>
}
