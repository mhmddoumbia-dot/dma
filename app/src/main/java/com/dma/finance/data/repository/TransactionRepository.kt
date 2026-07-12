package com.dma.finance.data.repository

import com.dma.finance.data.local.dao.TransactionDao
import com.dma.finance.data.local.entity.TransactionEntity
import com.dma.finance.data.local.relation.CategorySpending
import com.dma.finance.data.local.relation.MonthlySummary
import com.dma.finance.data.local.relation.TransactionWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

data class BalanceSummary(val incomeMinor: Long, val expenseMinor: Long) {
    val netMinor: Long get() = incomeMinor - expenseMinor
}

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun observeTransactionsForProject(projectId: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.observeTransactionsForProject(projectId)

    fun observeTransactionsForAccount(projectId: Long, accountId: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.observeTransactionsForAccount(projectId, accountId)

    fun observeSummary(projectId: Long, from: Long, to: Long): Flow<BalanceSummary> =
        combine(
            transactionDao.observeTotalIncome(projectId, from, to),
            transactionDao.observeTotalExpense(projectId, from, to)
        ) { income, expense -> BalanceSummary(income, expense) }

    fun observeExpenseByCategory(projectId: Long, from: Long, to: Long): Flow<List<CategorySpending>> =
        transactionDao.observeExpenseByCategory(projectId, from, to)

    fun observeMonthlySummaries(projectId: Long, from: Long, to: Long): Flow<List<MonthlySummary>> =
        transactionDao.observeMonthlySummaries(projectId, from, to)

    suspend fun addTransaction(transaction: TransactionEntity): Long = transactionDao.insert(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) = transactionDao.update(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) = transactionDao.delete(transaction)
}
