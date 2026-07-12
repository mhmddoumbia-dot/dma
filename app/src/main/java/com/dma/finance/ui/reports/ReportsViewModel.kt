package com.dma.finance.ui.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dma.finance.data.local.relation.CategorySpending
import com.dma.finance.data.local.relation.MonthlySummary
import com.dma.finance.data.repository.ProjectRepository
import com.dma.finance.data.repository.TransactionRepository
import com.dma.finance.util.PeriodUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    val projectId: Long = checkNotNull(savedStateHandle["projectId"])

    val currencyCode: StateFlow<String> = projectRepository.observeProject(projectId)
        .map { it?.currencyCode ?: "XOF" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "XOF")

    val expenseByCategory: StateFlow<List<CategorySpending>> = run {
        val (from, to) = PeriodUtils.currentMonthRange()
        transactionRepository.observeExpenseByCategory(projectId, from, to)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    val monthlySummaries: StateFlow<List<MonthlySummary>> = run {
        val (from, to) = PeriodUtils.lastMonthsRange(6)
        transactionRepository.observeMonthlySummaries(projectId, from, to)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}
