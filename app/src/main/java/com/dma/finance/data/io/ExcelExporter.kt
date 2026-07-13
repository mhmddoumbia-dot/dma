package com.dma.finance.data.io

import com.dma.finance.data.local.dao.AccountDao
import com.dma.finance.data.local.dao.BudgetDao
import com.dma.finance.data.local.dao.CategoryDao
import com.dma.finance.data.local.dao.TransactionDao
import kotlinx.coroutines.flow.first
import org.dhatim.fastexcel.Workbook
import java.io.OutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

/**
 * Exporte les données d'un projet (comptes, catégories, transactions, budgets) vers un
 * classeur Excel (.xlsx) à quatre feuilles, au format lisible par [ExcelImporter].
 */
class ExcelExporter @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    suspend fun export(projectId: Long, outputStream: OutputStream) {
        val accounts = accountDao.observeAccountsForProject(projectId).first()
        val categories = categoryDao.observeCategoriesForProject(projectId).first()
        val transactions = transactionDao.observeTransactionsForProject(projectId).first()
        val budgets = budgetDao.observeBudgetsForProject(projectId).first()
        val accountNameById = accounts.associate { it.id to it.name }
        val categoryNameById = categories.associate { it.id to it.name }

        val workbook = Workbook(outputStream, "Gestion Finances", "1.0")
        try {
            val accountsSheet = workbook.newWorksheet("Comptes")
            accountsSheet.value(0, 0, "Nom")
            accountsSheet.value(0, 1, "Type")
            accountsSheet.value(0, 2, "Solde initial")
            accounts.forEachIndexed { index, account ->
                val row = index + 1
                accountsSheet.value(row, 0, account.name)
                accountsSheet.value(row, 1, account.type.name)
                accountsSheet.value(row, 2, account.initialBalanceMinor / 100.0)
            }

            val categoriesSheet = workbook.newWorksheet("Catégories")
            categoriesSheet.value(0, 0, "Nom")
            categoriesSheet.value(0, 1, "Type")
            categoriesSheet.value(0, 2, "Couleur")
            categories.forEachIndexed { index, category ->
                val row = index + 1
                categoriesSheet.value(row, 0, category.name)
                categoriesSheet.value(row, 1, category.type.name)
                categoriesSheet.value(row, 2, category.colorHex)
            }

            val transactionsSheet = workbook.newWorksheet("Transactions")
            transactionsSheet.value(0, 0, "Date")
            transactionsSheet.value(0, 1, "Type")
            transactionsSheet.value(0, 2, "Compte")
            transactionsSheet.value(0, 3, "Catégorie")
            transactionsSheet.value(0, 4, "Montant")
            transactionsSheet.value(0, 5, "Note")
            transactionsSheet.value(0, 6, "Compte destination")
            transactions.forEachIndexed { index, transaction ->
                val row = index + 1
                val dateText = Instant.ofEpochMilli(transaction.date).atZone(ZoneId.systemDefault()).toLocalDate().format(ISO_DATE)
                transactionsSheet.value(row, 0, dateText)
                transactionsSheet.value(row, 1, transaction.type)
                transactionsSheet.value(row, 2, transaction.accountName)
                transactionsSheet.value(row, 3, transaction.categoryName ?: "")
                transactionsSheet.value(row, 4, transaction.amountMinor / 100.0)
                transactionsSheet.value(row, 5, transaction.note)
                val transferToName = transaction.transferToAccountId?.let { accountNameById[it] } ?: ""
                transactionsSheet.value(row, 6, transferToName)
            }

            val budgetsSheet = workbook.newWorksheet("Budgets")
            budgetsSheet.value(0, 0, "Catégorie")
            budgetsSheet.value(0, 1, "Montant limite")
            budgetsSheet.value(0, 2, "Période")
            budgetsSheet.value(0, 3, "Date de début")
            budgets.forEachIndexed { index, budget ->
                val row = index + 1
                val dateText = Instant.ofEpochMilli(budget.startDate).atZone(ZoneId.systemDefault()).toLocalDate().format(ISO_DATE)
                budgetsSheet.value(row, 0, categoryNameById[budget.categoryId] ?: "")
                budgetsSheet.value(row, 1, budget.amountLimitMinor / 100.0)
                budgetsSheet.value(row, 2, budget.period.name)
                budgetsSheet.value(row, 3, dateText)
            }
        } finally {
            workbook.close()
        }
    }
}
