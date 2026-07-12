package com.dma.finance.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
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
import com.dma.finance.ui.components.CategoryBreakdownList
import com.dma.finance.ui.components.MonthlyBarChart

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val currency by viewModel.currencyCode.collectAsState()
    val expenseByCategory by viewModel.expenseByCategory.collectAsState()
    val monthlySummaries by viewModel.monthlySummaries.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(stringResource(R.string.reports_by_month), style = MaterialTheme.typography.titleMedium)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                if (monthlySummaries.isEmpty()) {
                    Text(
                        text = stringResource(R.string.reports_no_data),
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    MonthlyBarChart(data = monthlySummaries, modifier = Modifier.padding(16.dp))
                }
            }
        }
        item {
            Text(stringResource(R.string.reports_by_category), style = MaterialTheme.typography.titleMedium)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                if (expenseByCategory.isEmpty()) {
                    Text(
                        text = stringResource(R.string.reports_no_data),
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    CategoryBreakdownList(
                        data = expenseByCategory,
                        currencyCode = currency,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
