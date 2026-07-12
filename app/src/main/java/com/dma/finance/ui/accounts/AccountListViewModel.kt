package com.dma.finance.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dma.finance.data.local.entity.AccountEntity
import com.dma.finance.data.repository.AccountRepository
import com.dma.finance.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    projectRepository: ProjectRepository
) : ViewModel() {

    val projectId: Long = checkNotNull(savedStateHandle["projectId"])

    val accounts: StateFlow<List<AccountEntity>> = accountRepository.observeAccountsForProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balances: StateFlow<Map<Long, Long>> = accounts.flatMapLatest { list ->
        if (list.isEmpty()) {
            flowOf(emptyMap())
        } else {
            combine(list.map { account -> accountRepository.observeBalance(account.id).map { account.id to (it ?: 0L) } }) { pairs ->
                pairs.toMap()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val currencyCode: StateFlow<String> = projectRepository.observeProject(projectId)
        .map { it?.currencyCode ?: "XOF" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "XOF")

    fun archiveAccount(account: AccountEntity) {
        viewModelScope.launch { accountRepository.archiveAccount(account) }
    }
}
