package com.dma.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dma.finance.data.local.entity.TransactionEntity
import com.dma.finance.data.local.relation.CategorySpending
import com.dma.finance.data.local.relation.MonthlySummary
import com.dma.finance.data.local.relation.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun findById(transactionId: Long): TransactionEntity?

    @Query(
        """
        SELECT
            t.id, t.projectId, t.accountId, a.name AS accountName,
            t.categoryId, c.name AS categoryName, c.icon AS categoryIcon, c.colorHex AS categoryColor,
            t.recordedByUserId, u.fullName AS recordedByName,
            t.type, t.amountMinor, t.date, t.note, t.transferToAccountId
        FROM transactions t
        INNER JOIN accounts a ON a.id = t.accountId
        LEFT JOIN categories c ON c.id = t.categoryId
        INNER JOIN users u ON u.id = t.recordedByUserId
        WHERE t.projectId = :projectId
        ORDER BY t.date DESC, t.id DESC
        """
    )
    fun observeTransactionsForProject(projectId: Long): Flow<List<TransactionWithDetails>>

    @Query(
        """
        SELECT
            t.id, t.projectId, t.accountId, a.name AS accountName,
            t.categoryId, c.name AS categoryName, c.icon AS categoryIcon, c.colorHex AS categoryColor,
            t.recordedByUserId, u.fullName AS recordedByName,
            t.type, t.amountMinor, t.date, t.note, t.transferToAccountId
        FROM transactions t
        INNER JOIN accounts a ON a.id = t.accountId
        LEFT JOIN categories c ON c.id = t.categoryId
        INNER JOIN users u ON u.id = t.recordedByUserId
        WHERE t.projectId = :projectId AND t.accountId = :accountId
        ORDER BY t.date DESC, t.id DESC
        """
    )
    fun observeTransactionsForAccount(projectId: Long, accountId: Long): Flow<List<TransactionWithDetails>>

    @Query(
        "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions WHERE projectId = :projectId AND type = 'INCOME' AND date BETWEEN :from AND :to"
    )
    fun observeTotalIncome(projectId: Long, from: Long, to: Long): Flow<Long>

    @Query(
        "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions WHERE projectId = :projectId AND type = 'EXPENSE' AND date BETWEEN :from AND :to"
    )
    fun observeTotalExpense(projectId: Long, from: Long, to: Long): Flow<Long>

    @Query(
        "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions WHERE projectId = :projectId AND categoryId = :categoryId AND type = 'EXPENSE' AND date BETWEEN :from AND :to"
    )
    suspend fun sumExpenseForCategory(projectId: Long, categoryId: Long, from: Long, to: Long): Long

    @Query(
        "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions WHERE projectId = :projectId AND categoryId = :categoryId AND type = 'EXPENSE' AND date BETWEEN :from AND :to"
    )
    fun observeSumExpenseForCategory(projectId: Long, categoryId: Long, from: Long, to: Long): Flow<Long>

    @Query(
        """
        SELECT c.id AS categoryId, c.name AS categoryName, c.colorHex AS colorHex,
               COALESCE(SUM(t.amountMinor), 0) AS totalMinor
        FROM categories c
        LEFT JOIN transactions t ON t.categoryId = c.id AND t.type = 'EXPENSE' AND t.date BETWEEN :from AND :to
        WHERE c.projectId = :projectId AND c.type = 'EXPENSE'
        GROUP BY c.id
        HAVING totalMinor > 0
        ORDER BY totalMinor DESC
        """
    )
    fun observeExpenseByCategory(projectId: Long, from: Long, to: Long): Flow<List<CategorySpending>>

    @Query(
        """
        SELECT strftime('%Y-%m', date / 1000, 'unixepoch') AS yearMonth,
               COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountMinor ELSE 0 END), 0) AS incomeMinor,
               COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountMinor ELSE 0 END), 0) AS expenseMinor
        FROM transactions
        WHERE projectId = :projectId AND date BETWEEN :from AND :to
        GROUP BY yearMonth
        ORDER BY yearMonth ASC
        """
    )
    fun observeMonthlySummaries(projectId: Long, from: Long, to: Long): Flow<List<MonthlySummary>>
}
