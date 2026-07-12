package com.dma.finance.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dma.finance.R
import com.dma.finance.data.local.entity.TransactionType
import com.dma.finance.ui.components.AmountText
import com.dma.finance.ui.components.TransactionRow
import com.dma.finance.util.CurrencyFormatter

@Composable
fun DashboardScreen(
    onOpenTransaction: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val project by viewModel.project.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val monthSummary by viewModel.monthSummary.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val currency = project?.currencyCode ?: "XOF"

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.dashboard_total_balance),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = CurrencyFormatter.format(totalBalance, currency),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(stringResource(R.string.dashboard_income), style = MaterialTheme.typography.bodyMedium)
                            AmountText(monthSummary.incomeMinor, currency, TransactionType.INCOME)
                        }
                        Column {
                            Text(stringResource(R.string.dashboard_expenses), style = MaterialTheme.typography.bodyMedium)
                            AmountText(monthSummary.expenseMinor, currency, TransactionType.EXPENSE)
                        }
                    }
                }
            }
        }

        if (accounts.isNotEmpty()) {
            item {
                Text(stringResource(R.string.dashboard_accounts), style = MaterialTheme.typography.titleMedium)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(accounts, key = { it.id }) { account ->
                        Card(
                            modifier = Modifier.size(width = 160.dp, height = 90.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = account.name, style = MaterialTheme.typography.bodyLarge)
                                Text(text = account.type.name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.dashboard_recent_transactions), style = MaterialTheme.typography.titleMedium)
        }

        if (recentTransactions.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.dashboard_no_transactions),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(recentTransactions, key = { it.id }) { transaction ->
                TransactionRow(
                    transaction = transaction,
                    currencyCode = currency,
                    onClick = { onOpenTransaction(transaction.id) }
                )
            }
        }
    }
}
