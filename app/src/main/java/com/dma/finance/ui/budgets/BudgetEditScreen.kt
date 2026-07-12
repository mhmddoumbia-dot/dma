package com.dma.finance.ui.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dma.finance.R
import com.dma.finance.data.local.entity.BudgetPeriod
import com.dma.finance.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditScreen(
    onBack: () -> Unit,
    viewModel: BudgetEditViewModel = hiltViewModel()
) {
    val categories by viewModel.expenseCategories.collectAsState()
    val saved by viewModel.saved.collectAsState()

    var categoryId by remember { mutableStateOf(0L) }
    var amountText by remember { mutableStateOf("") }
    var period by remember { mutableStateOf(BudgetPeriod.MONTHLY) }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var periodMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(categories) {
        if (categoryId == 0L && categories.isNotEmpty()) categoryId = categories.first().id
    }

    LaunchedEffect(Unit) {
        viewModel.loadExisting { budget ->
            categoryId = budget.categoryId
            amountText = (budget.amountLimitMinor / 100.0).toString()
            period = budget.period
            startDate = budget.startDate
        }
    }

    LaunchedEffect(saved) {
        if (saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (viewModel.isEditing) R.string.action_edit else R.string.budgets_add)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (viewModel.isEditing) {
                        IconButton(onClick = { viewModel.delete() }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val selectedCategoryName = categories.firstOrNull { it.id == categoryId }?.name ?: ""
            ExposedDropdownMenuBox(expanded = categoryMenuExpanded, onExpandedChange = { categoryMenuExpanded = it }) {
                OutlinedTextField(
                    value = selectedCategoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.transactions_category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                androidx.compose.material3.ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                categoryId = category.id
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(stringResource(R.string.budgets_amount_limit)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = periodMenuExpanded, onExpandedChange = { periodMenuExpanded = it }) {
                OutlinedTextField(
                    value = periodLabel(period),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.budgets_period)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                androidx.compose.material3.ExposedDropdownMenu(
                    expanded = periodMenuExpanded,
                    onDismissRequest = { periodMenuExpanded = false }
                ) {
                    BudgetPeriod.values().forEach { p ->
                        DropdownMenuItem(
                            text = { Text(periodLabel(p)) },
                            onClick = {
                                period = p
                                periodMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val minor = CurrencyFormatter.parseToMinor(amountText) ?: return@Button
                    viewModel.save(categoryId, minor, period, startDate)
                },
                enabled = categoryId > 0 && amountText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}

@Composable
private fun periodLabel(period: BudgetPeriod): String = when (period) {
    BudgetPeriod.WEEKLY -> stringResource(R.string.budgets_period_weekly)
    BudgetPeriod.MONTHLY -> stringResource(R.string.budgets_period_monthly)
    BudgetPeriod.YEARLY -> stringResource(R.string.budgets_period_yearly)
}
