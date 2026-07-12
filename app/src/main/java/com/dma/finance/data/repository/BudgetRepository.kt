package com.dma.finance.data.repository

import com.dma.finance.data.local.dao.BudgetDao
import com.dma.finance.data.local.dao.CategoryDao
import com.dma.finance.data.local.dao.TransactionDao
import com.dma.finance.data.local.entity.BudgetEntity
import com.dma.finance.util.PeriodUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Un budget accompagné du montant déjà dépensé sur sa période courante. */
data class BudgetWithProgress(
    val budget: BudgetEntity,
    val categoryName: String,
    val categoryColorHex: String,
    val spentMinor: Long
) {
    val progress: Float
        get() = if (budget.amountLimitMinor <= 0) 0f
        else (spentMinor.toFloat() / budget.amountLimitMinor.toFloat()).coerceIn(0f, 5f)

    val isExceeded: Boolean get() = spentMinor > budget.amountLimitMinor
}

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    fun observeBudgetsForProject(projectId: Long): Flow<List<BudgetEntity>> =
        budgetDao.observeBudgetsForProject(projectId)

    /** Combine chaque budget avec sa catégorie et le montant déjà dépensé sur la période en cours. */
    fun observeBudgetsWithProgress(projectId: Long): Flow<List<BudgetWithProgress>> =
        budgetDao.observeBudgetsForProject(projectId).flatMapLatest { budgets ->
            if (budgets.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                val flows = budgets.map { budget ->
                    val periodEnd = PeriodUtils.periodEndMillis(budget.startDate, budget.period)
                    transactionDao.observeSumExpenseForCategory(projectId, budget.categoryId, budget.startDate, periodEnd)
                        .map { spent -> budget to spent }
                }
                combine(flows) { pairs ->
                    pairs.map { (budget, spent) ->
                        val category = categoryDao.findById(budget.categoryId)
                        BudgetWithProgress(
                            budget = budget,
                            categoryName = category?.name ?: "",
                            categoryColorHex = category?.colorHex ?: "#616161",
                            spentMinor = spent
                        )
                    }
                }
            }
        }

    suspend fun createBudget(budget: BudgetEntity): Long = budgetDao.insert(budget)

    suspend fun updateBudget(budget: BudgetEntity) = budgetDao.update(budget)

    suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.delete(budget)
}
