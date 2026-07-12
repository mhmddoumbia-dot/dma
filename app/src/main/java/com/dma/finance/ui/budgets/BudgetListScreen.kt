package com.dma.finance.ui.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dma.finance.R
import com.dma.finance.data.repository.BudgetWithProgress
import com.dma.finance.ui.components.parseColor
import com.dma.finance.ui.theme.ExpenseRed
import com.dma.finance.util.CurrencyFormatter

@Composable
fun BudgetListScreen(
    onAddBudget: () -> Unit,
    onEditBudget: (Long) -> Unit,
    viewModel: BudgetListViewModel = hiltViewModel()
) {
    val budgets by viewModel.budgets.collectAsState()
    val currency by viewModel.currencyCode.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBudget) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.budgets_add))
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(budgets, key = { it.budget.id }) { item ->
                BudgetCard(item = item, currencyCode = currency, onClick = { onEditBudget(item.budget.id) })
            }
        }
    }
}

@Composable
private fun BudgetCard(item: BudgetWithProgress, currencyCode: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = item.categoryName, style = MaterialTheme.typography.bodyLarge)
                Text(text = item.budget.period.name, style = MaterialTheme.typography.bodyMedium)
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { item.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (item.isExceeded) ExpenseRed else parseColor(item.categoryColorHex),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(
                    R.string.budgets_spent_of,
                    CurrencyFormatter.format(item.spentMinor, currencyCode),
                    CurrencyFormatter.format(item.budget.amountLimitMinor, currencyCode)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            if (item.isExceeded) {
                Text(
                    text = stringResource(R.string.budgets_exceeded),
                    color = ExpenseRed,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
