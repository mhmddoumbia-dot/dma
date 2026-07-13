package com.dma.finance.data.io

import com.dma.finance.data.local.dao.AccountDao
import com.dma.finance.data.local.dao.BudgetDao
import com.dma.finance.data.local.dao.CategoryDao
import com.dma.finance.data.local.dao.TransactionDao
import com.dma.finance.data.local.entity.AccountEntity
import com.dma.finance.data.local.entity.AccountType
import com.dma.finance.data.local.entity.BudgetEntity
import com.dma.finance.data.local.entity.BudgetPeriod
import com.dma.finance.data.local.entity.CategoryEntity
import com.dma.finance.data.local.entity.CategoryType
import com.dma.finance.data.local.entity.TransactionEntity
import com.dma.finance.data.local.entity.TransactionType
import kotlinx.coroutines.flow.first
import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.Row
import org.dhatim.fastexcel.reader.Sheet
import java.io.InputStream
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/** Résultat d'un import : nombre d'éléments créés/importés et lignes ignorées (données invalides). */
data class ImportSummary(
    val accountsCreated: Int = 0,
    val categoriesCreated: Int = 0,
    val transactionsImported: Int = 0,
    val budgetsImported: Int = 0,
    val rowsSkipped: Int = 0
)

/**
 * Importe un classeur Excel (.xlsx) produit par [ExcelExporter], ou respectant le même
 * format de colonnes, dans un projet existant. Les comptes et catégories référencés par
 * nom sont créés automatiquement s'ils n'existent pas déjà dans le projet.
 */
class ExcelImporter @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    suspend fun import(projectId: Long, recordedByUserId: Long, inputStream: InputStream): ImportSummary {
        var accountsCreated = 0
        var categoriesCreated = 0
        var transactionsImported = 0
        var budgetsImported = 0
        var rowsSkipped = 0

        val accountsByName = accountDao.observeAccountsForProject(projectId).first()
            .associateBy { it.name.trim().lowercase() }
            .toMutableMap()
        val categoriesByName = categoryDao.observeCategoriesForProject(projectId).first()
            .associateBy { it.name.trim().lowercase() }
            .toMutableMap()

        suspend fun resolveAccount(name: String, type: AccountType = AccountType.OTHER): AccountEntity {
            val trimmed = name.trim().ifEmpty { "Compte importé" }
            val key = trimmed.lowercase()
            accountsByName[key]?.let { return it }
            val id = accountDao.insert(AccountEntity(projectId = projectId, name = trimmed, type = type))
            val created = AccountEntity(id = id, projectId = projectId, name = trimmed, type = type)
            accountsByName[key] = created
            accountsCreated++
            return created
        }

        suspend fun resolveCategory(name: String, type: CategoryType, colorHex: String = "#616161"): CategoryEntity? {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return null
            val key = trimmed.lowercase()
            categoriesByName[key]?.let { return it }
            val id = categoryDao.insert(CategoryEntity(projectId = projectId, name = trimmed, type = type, colorHex = colorHex))
            val created = CategoryEntity(id = id, projectId = projectId, name = trimmed, type = type, colorHex = colorHex)
            categoriesByName[key] = created
            categoriesCreated++
            return created
        }

        ReadableWorkbook(inputStream).use { workbook ->
            val sheets = mutableListOf<Sheet>()
            workbook.getSheets().forEach { sheets.add(it) }

            sheets.firstOrNull { it.name == "Comptes" }?.let { sheet ->
                readRows(sheet).forEach { row ->
                    val name = row.getCellText(0).trim()
                    if (name.isEmpty()) {
                        rowsSkipped++
                    } else {
                        val type = row.getCellText(1).trim().uppercase().toAccountTypeOrNull() ?: AccountType.OTHER
                        resolveAccount(name, type)
                    }
                }
            }

            sheets.firstOrNull { it.name == "Catégories" }?.let { sheet ->
                readRows(sheet).forEach { row ->
                    val name = row.getCellText(0).trim()
                    val type = row.getCellText(1).trim().uppercase().toCategoryTypeOrNull()
                    if (name.isEmpty() || type == null) {
                        rowsSkipped++
                    } else {
                        val color = row.getCellText(2).trim().ifEmpty { "#616161" }
                        resolveCategory(name, type, color)
                    }
                }
            }

            sheets.firstOrNull { it.name == "Transactions" }?.let { sheet ->
                for (row in readRows(sheet)) {
                    val type = row.getCellText(1).trim().uppercase().toTransactionTypeOrNull()
                    val amountMinor = row.getCellText(4).trim().replace(",", ".").toDoubleOrNull()?.let { Math.round(it * 100) }
                    val accountName = row.getCellText(2).trim()
                    if (type == null || amountMinor == null || amountMinor <= 0 || accountName.isEmpty()) {
                        rowsSkipped++
                        continue
                    }
                    val account = resolveAccount(accountName)
                    val category = if (type == TransactionType.TRANSFER) {
                        null
                    } else {
                        val categoryType = if (type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
                        resolveCategory(row.getCellText(3).trim(), categoryType)
                    }
                    val transferToName = row.getCellText(6).trim()
                    val transferToAccount = if (type == TransactionType.TRANSFER && transferToName.isNotEmpty()) {
                        resolveAccount(transferToName)
                    } else null

                    transactionDao.insert(
                        TransactionEntity(
                            projectId = projectId,
                            accountId = account.id,
                            categoryId = category?.id,
                            recordedByUserId = recordedByUserId,
                            type = type,
                            amountMinor = amountMinor,
                            date = parseDateMillis(row.getCellText(0)),
                            note = row.getCellText(5),
                            transferToAccountId = transferToAccount?.id
                        )
                    )
                    transactionsImported++
                }
            }

            sheets.firstOrNull { it.name == "Budgets" }?.let { sheet ->
                for (row in readRows(sheet)) {
                    val categoryName = row.getCellText(0).trim()
                    val amountMinor = row.getCellText(1).trim().replace(",", ".").toDoubleOrNull()?.let { Math.round(it * 100) }
                    val period = row.getCellText(2).trim().uppercase().toBudgetPeriodOrNull()
                    if (categoryName.isEmpty() || amountMinor == null || amountMinor <= 0 || period == null) {
                        rowsSkipped++
                        continue
                    }
                    val category = resolveCategory(categoryName, CategoryType.EXPENSE)
                    if (category == null) {
                        rowsSkipped++
                        continue
                    }
                    budgetDao.insert(
                        BudgetEntity(
                            projectId = projectId,
                            categoryId = category.id,
                            amountLimitMinor = amountMinor,
                            period = period,
                            startDate = parseDateMillis(row.getCellText(3))
                        )
                    )
                    budgetsImported++
                }
            }
        }

        return ImportSummary(accountsCreated, categoriesCreated, transactionsImported, budgetsImported, rowsSkipped)
    }

    /** Lit toutes les lignes d'une feuille (sauf l'en-tête) en mémoire, dans l'ordre. */
    private fun readRows(sheet: Sheet): List<Row> {
        val rows = mutableListOf<Row>()
        sheet.openStream().use { stream ->
            stream.skip(1).forEach { rows.add(it) }
        }
        return rows
    }

    private fun parseDateMillis(text: String): Long {
        val trimmed = text.trim()
        val date = try {
            LocalDate.parse(trimmed)
        } catch (e: Exception) {
            LocalDate.now()
        }
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun String.toAccountTypeOrNull(): AccountType? = try {
        AccountType.valueOf(this)
    } catch (e: IllegalArgumentException) {
        null
    }

    private fun String.toCategoryTypeOrNull(): CategoryType? = try {
        CategoryType.valueOf(this)
    } catch (e: IllegalArgumentException) {
        null
    }

    private fun String.toTransactionTypeOrNull(): TransactionType? = try {
        TransactionType.valueOf(this)
    } catch (e: IllegalArgumentException) {
        null
    }

    private fun String.toBudgetPeriodOrNull(): BudgetPeriod? = try {
        BudgetPeriod.valueOf(this)
    } catch (e: IllegalArgumentException) {
        null
    }
}
