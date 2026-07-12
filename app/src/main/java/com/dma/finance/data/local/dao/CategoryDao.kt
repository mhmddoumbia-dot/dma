package com.dma.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dma.finance.data.local.entity.CategoryEntity
import com.dma.finance.data.local.entity.CategoryType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE projectId = :projectId ORDER BY name ASC")
    fun observeCategoriesForProject(projectId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE projectId = :projectId AND type = :type ORDER BY name ASC")
    fun observeCategoriesByType(projectId: Long, type: CategoryType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun findById(categoryId: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories WHERE projectId = :projectId")
    suspend fun countForProject(projectId: Long): Int
}
