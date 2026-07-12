package com.dma.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dma.finance.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE projectId = :projectId ORDER BY startDate DESC")
    fun observeBudgetsForProject(projectId: Long): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :budgetId LIMIT 1")
    suspend fun findById(budgetId: Long): BudgetEntity?
}
