package com.dma.finance.data.repository

import com.dma.finance.data.local.dao.CategoryDao
import com.dma.finance.data.local.entity.CategoryEntity
import com.dma.finance.data.local.entity.CategoryType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun observeCategoriesForProject(projectId: Long): Flow<List<CategoryEntity>> =
        categoryDao.observeCategoriesForProject(projectId)

    fun observeCategoriesByType(projectId: Long, type: CategoryType): Flow<List<CategoryEntity>> =
        categoryDao.observeCategoriesByType(projectId, type)

    suspend fun createCategory(category: CategoryEntity): Long = categoryDao.insert(category)

    suspend fun updateCategory(category: CategoryEntity) = categoryDao.update(category)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)
}
