package com.dma.finance.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dma.finance.data.local.entity.AccountEntity
import com.dma.finance.data.local.entity.CategoryEntity
import com.dma.finance.data.local.entity.TransactionEntity
import com.dma.finance.data.local.entity.TransactionType
import com.dma.finance.data.repository.AccountRepository
import com.dma.finance.data.repository.AuthRepository
import com.dma.finance.data.repository.CategoryRepository
import com.dma.finance.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val projectId: Long = checkNotNull(savedStateHandle["projectId"])
    private val transactionId: Long? = (savedStateHandle.get<Long>("transactionId"))?.takeIf { it > 0 }
    val isEditing: Boolean get() = transactionId != null

    val accounts: StateFlow<List<AccountEntity>> = accountRepository.observeAccountsForProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeCategoriesForProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun loadExisting(onLoaded: (TransactionEntity) -> Unit) {
        val id = transactionId ?: return
        viewModelScope.launch {
            val existing = transactionRepository.observeTransactionsForProject(projectId).first()
                .firstOrNull { it.id == id }
            existing ?: return@launch
            onLoaded(
                TransactionEntity(
                    id = existing.id,
                    projectId = projectId,
                    accountId = existing.accountId,
                    categoryId = existing.categoryId,
                    recordedByUserId = existing.recordedByUserId,
                    type = TransactionType.valueOf(existing.type),
                    amountMinor = existing.amountMinor,
                    date = existing.date,
                    note = existing.note,
                    transferToAccountId = existing.transferToAccountId
                )
            )
        }
    }

    fun save(
        type: TransactionType,
        accountId: Long,
        categoryId: Long?,
        amountMinor: Long,
        date: Long,
        note: String,
        transferToAccountId: Long?
    ) {
        if (accountId <= 0 || amountMinor <= 0) return
        viewModelScope.launch {
            val userId = authRepository.currentUser.first()?.id ?: return@launch
            val transaction = TransactionEntity(
                id = transactionId ?: 0L,
                projectId = projectId,
                accountId = accountId,
                categoryId = if (type == TransactionType.TRANSFER) null else categoryId,
                recordedByUserId = userId,
                type = type,
                amountMinor = amountMinor,
                date = date,
                note = note,
                transferToAccountId = if (type == TransactionType.TRANSFER) transferToAccountId else null
            )
            if (isEditing) {
                transactionRepository.updateTransaction(transaction)
            } else {
                transactionRepository.addTransaction(transaction)
            }
            _saved.value = true
        }
    }

    fun delete() {
        val id = transactionId ?: return
        viewModelScope.launch {
            val existing = transactionRepository.observeTransactionsForProject(projectId).first()
                .firstOrNull { it.id == id } ?: return@launch
            transactionRepository.deleteTransaction(
                TransactionEntity(
                    id = existing.id,
                    projectId = projectId,
                    accountId = existing.accountId,
                    categoryId = existing.categoryId,
                    recordedByUserId = existing.recordedByUserId,
                    type = TransactionType.valueOf(existing.type),
                    amountMinor = existing.amountMinor,
                    date = existing.date,
                    note = existing.note,
                    transferToAccountId = existing.transferToAccountId
                )
            )
            _saved.value = true
        }
    }
}
