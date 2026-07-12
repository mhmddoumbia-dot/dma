package com.dma.finance.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dma.finance.data.local.entity.AccountEntity
import com.dma.finance.data.local.entity.AccountType
import com.dma.finance.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository
) : ViewModel() {

    val projectId: Long = checkNotNull(savedStateHandle["projectId"])
    private val accountId: Long? = (savedStateHandle.get<Long>("accountId"))?.takeIf { it > 0 }

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    val isEditing: Boolean get() = accountId != null

    fun loadExisting(onLoaded: (AccountEntity) -> Unit) {
        val id = accountId ?: return
        viewModelScope.launch {
            val account = accountRepository.observeAccount(id).filterNotNull().first()
            onLoaded(account)
        }
    }

    fun save(name: String, type: AccountType, initialBalanceMinor: Long) {
        if (name.isBlank()) return
        viewModelScope.launch {
            if (isEditing && accountId != null) {
                accountRepository.updateAccount(
                    AccountEntity(id = accountId, projectId = projectId, name = name, type = type, initialBalanceMinor = initialBalanceMinor)
                )
            } else {
                accountRepository.createAccount(
                    AccountEntity(projectId = projectId, name = name, type = type, initialBalanceMinor = initialBalanceMinor)
                )
            }
            _saved.value = true
        }
    }
}
