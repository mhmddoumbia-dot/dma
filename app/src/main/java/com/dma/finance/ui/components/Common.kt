package com.dma.finance.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dma.finance.data.local.entity.TransactionType
import com.dma.finance.ui.theme.ExpenseRed
import com.dma.finance.ui.theme.IncomeGreen
import com.dma.finance.util.CurrencyFormatter

/** Affiche un montant coloré selon qu'il s'agit d'un revenu ou d'une dépense. */
@Composable
fun AmountText(
    amountMinor: Long,
    currencyCode: String,
    type: TransactionType,
    modifier: Modifier = Modifier
) {
    val (prefix, color) = when (type) {
        TransactionType.INCOME -> "+" to IncomeGreen
        TransactionType.EXPENSE -> "-" to ExpenseRed
        TransactionType.TRANSFER -> "" to MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = "$prefix${CurrencyFormatter.format(amountMinor, currencyCode)}",
        color = color,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
    )
}

fun parseColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: IllegalArgumentException) {
    Color.Gray
}
