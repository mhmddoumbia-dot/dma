package com.dma.finance.data.repository

import com.dma.finance.data.local.dao.AccountDao
import com.dma.finance.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao
) {
    fun observeAccountsForProject(projectId: Long): Flow<List<AccountEntity>> =
        accountDao.observeAccountsForProject(projectId)

    fun observeAccount(accountId: Long): Flow<AccountEntity?> = accountDao.observeById(accountId)

    fun observeBalance(accountId: Long): Flow<Long?> = accountDao.observeCurrentBalance(accountId)

    /** Somme des soldes courants de tous les comptes actifs d'un projet. */
    fun observeTotalBalanceForProject(projectId: Long): Flow<Long> =
        accountDao.observeAccountsForProject(projectId).flatMapLatest { accounts ->
            if (accounts.isEmpty()) {
                flowOf(0L)
            } else {
                combine(accounts.map { accountDao.observeCurrentBalance(it.id) }) { balances ->
                    balances.sumOf { it ?: 0L }
                }
            }
        }

    suspend fun createAccount(account: AccountEntity): Long = accountDao.insert(account)

    suspend fun updateAccount(account: AccountEntity) = accountDao.update(account)

    suspend fun archiveAccount(account: AccountEntity) = accountDao.update(account.copy(archived = true))

    suspend fun deleteAccount(account: AccountEntity) = accountDao.delete(account)
}
