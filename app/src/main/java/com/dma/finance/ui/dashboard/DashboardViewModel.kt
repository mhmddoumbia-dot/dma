package com.dma.finance.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dma.finance.data.local.entity.AccountEntity
import com.dma.finance.data.local.entity.ProjectEntity
import com.dma.finance.data.local.relation.TransactionWithDetails
import com.dma.finance.data.repository.AccountRepository
import com.dma.finance.data.repository.BalanceSummary
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
class DashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val projectRepository: ProjectRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val projectId: Long = checkNotNull(savedStateHandle["projectId"])

    val project: StateFlow<ProjectEntity?> = projectRepository.observeProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val accounts: StateFlow<List<AccountEntity>> = accountRepository.observeAccountsForProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBalance: StateFlow<Long> = accountRepository.observeTotalBalanceForProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthSummary: StateFlow<BalanceSummary> = run {
        val (from, to) = PeriodUtils.currentMonthRange()
        transactionRepository.observeSummary(projectId, from, to)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BalanceSummary(0, 0))
    }

    val recentTransactions: StateFlow<List<TransactionWithDetails>> =
        transactionRepository.observeTransactionsForProject(projectId)
            .map { it.take(5) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
