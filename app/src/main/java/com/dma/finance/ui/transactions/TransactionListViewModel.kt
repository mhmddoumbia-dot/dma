package com.dma.finance.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dma.finance.data.local.relation.TransactionWithDetails
import com.dma.finance.data.repository.ProjectRepository
import com.dma.finance.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    val projectId: Long = checkNotNull(savedStateHandle["projectId"])

    val transactions: StateFlow<List<TransactionWithDetails>> =
        transactionRepository.observeTransactionsForProject(projectId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currencyCode: StateFlow<String> = projectRepository.observeProject(projectId)
        .map { it?.currencyCode ?: "XOF" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "XOF")
}
