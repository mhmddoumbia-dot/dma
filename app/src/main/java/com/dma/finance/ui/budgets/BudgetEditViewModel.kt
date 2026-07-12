package com.dma.finance.ui.budgets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dma.finance.data.local.entity.BudgetEntity
import com.dma.finance.data.local.entity.BudgetPeriod
import com.dma.finance.data.local.entity.CategoryEntity
import com.dma.finance.data.local.entity.CategoryType
import com.dma.finance.data.repository.BudgetRepository
import com.dma.finance.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    val projectId: Long = checkNotNull(savedStateHandle["projectId"])
    private val budgetId: Long? = (savedStateHandle.get<Long>("budgetId"))?.takeIf { it > 0 }
    val isEditing: Boolean get() = budgetId != null

    val expenseCategories: StateFlow<List<CategoryEntity>> =
        categoryRepository.observeCategoriesByType(projectId, CategoryType.EXPENSE)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun loadExisting(onLoaded: (BudgetEntity) -> Unit) {
        val id = budgetId ?: return
        viewModelScope.launch {
            val budget = budgetRepository.observeBudgetsForProject(projectId).first()
                .firstOrNull { it.id == id }
            budget?.let(onLoaded)
        }
    }

    fun save(categoryId: Long, amountLimitMinor: Long, period: BudgetPeriod, startDate: Long) {
        if (categoryId <= 0 || amountLimitMinor <= 0) return
        viewModelScope.launch {
            if (isEditing && budgetId != null) {
                budgetRepository.updateBudget(
                    BudgetEntity(id = budgetId, projectId = projectId, categoryId = categoryId, amountLimitMinor = amountLimitMinor, period = period, startDate = startDate)
                )
            } else {
                budgetRepository.createBudget(
                    BudgetEntity(projectId = projectId, categoryId = categoryId, amountLimitMinor = amountLimitMinor, period = period, startDate = startDate)
                )
            }
            _saved.value = true
        }
    }

    fun delete() {
        val id = budgetId ?: return
        viewModelScope.launch {
            val budget = budgetRepository.observeBudgetsForProject(projectId).first().firstOrNull { it.id == id } ?: return@launch
            budgetRepository.deleteBudget(budget)
            _saved.value = true
        }
    }
}
