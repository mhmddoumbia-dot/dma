package com.dma.finance.ui.budgets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dma.finance.data.repository.BudgetRepository
import com.dma.finance.data.repository.BudgetWithProgress
import com.dma.finance.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BudgetListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val budgetRepository: BudgetRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    val projectId: Long = checkNotNull(savedStateHandle["projectId"])

    val budgets: StateFlow<List<BudgetWithProgress>> = budgetRepository.observeBudgetsWithProgress(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currencyCode: StateFlow<String> = projectRepository.observeProject(projectId)
        .map { it?.currencyCode ?: "XOF" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "XOF")
}
