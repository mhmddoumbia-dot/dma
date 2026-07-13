package com.dma.finance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dma.finance.data.local.entity.TransactionType
import com.dma.finance.data.local.relation.TransactionWithDetails
import com.dma.finance.util.DateUtils

@Composable
fun TransactionRow(
    transaction: TransactionWithDetails,
    currencyCode: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = parseColor(transaction.categoryColor ?: "#616161").let { it.copy(alpha = 0.18f) },
                            shape = CircleShape
                        )
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = transaction.categoryName ?: "Virement",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${transaction.accountName} · ${DateUtils.formatDate(transaction.date)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (transaction.note.isNotBlank()) {
                        Text(text = transaction.note, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            AmountText(
                amountMinor = transaction.amountMinor,
                currencyCode = currencyCode,
                type = TransactionType.valueOf(transaction.type)
            )
        }
    }
}
