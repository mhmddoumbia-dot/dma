package com.dma.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dma.finance.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE projectId = :projectId AND archived = 0 ORDER BY createdAt ASC")
    fun observeAccountsForProject(projectId: Long): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    suspend fun findById(accountId: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    fun observeById(accountId: Long): Flow<AccountEntity?>

    /** Solde courant = solde initial + revenus - dépenses - virements sortants + virements entrants. */
    @Query(
        """
        SELECT a.initialBalanceMinor
            + COALESCE((SELECT SUM(t.amountMinor) FROM transactions t WHERE t.accountId = a.id AND t.type = 'INCOME'), 0)
            - COALESCE((SELECT SUM(t.amountMinor) FROM transactions t WHERE t.accountId = a.id AND t.type = 'EXPENSE'), 0)
            - COALESCE((SELECT SUM(t.amountMinor) FROM transactions t WHERE t.accountId = a.id AND t.type = 'TRANSFER'), 0)
            + COALESCE((SELECT SUM(t.amountMinor) FROM transactions t WHERE t.transferToAccountId = a.id AND t.type = 'TRANSFER'), 0)
        FROM accounts a WHERE a.id = :accountId
        """
    )
    fun observeCurrentBalance(accountId: Long): Flow<Long?>
}
