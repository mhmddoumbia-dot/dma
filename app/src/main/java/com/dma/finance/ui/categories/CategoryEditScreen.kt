package com.dma.finance.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dma.finance.R
import com.dma.finance.data.local.entity.CategoryType
import com.dma.finance.ui.components.parseColor

private val CATEGORY_COLORS = listOf(
    "#2E7D32", "#558B2F", "#EF6C00", "#6D4C41", "#5E35B1",
    "#D81B60", "#1565C0", "#00838F", "#616161", "#C62828"
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditScreen(
    onBack: () -> Unit,
    viewModel: CategoryEditViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(CategoryType.EXPENSE) }
    var colorHex by remember { mutableStateOf(CATEGORY_COLORS.first()) }
    val saved by viewModel.saved.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadExisting { category ->
            name = category.name
            type = category.type
            colorHex = category.colorHex
        }
    }

    LaunchedEffect(saved) {
        if (saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (viewModel.isEditing) R.string.action_edit else R.string.categories_add)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.categories_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CategoryType.values().forEachIndexed { index, categoryType ->
                    SegmentedButton(
                        selected = type == categoryType,
                        onClick = { type = categoryType },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = CategoryType.values().size)
                    ) {
                        Text(if (categoryType == CategoryType.INCOME) stringResource(R.string.transactions_type_income) else stringResource(R.string.transactions_type_expense))
                    }
                }
            }

            Text(stringResource(R.string.categories_color), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CATEGORY_COLORS.forEach { hex ->
                    val selected = hex == colorHex
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(color = parseColor(hex), shape = CircleShape)
                            .then(
                                if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            )
                            .clickable { colorHex = hex }
                    )
                }
            }

            Button(
                onClick = { viewModel.save(name, type, colorHex, "category") },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}
