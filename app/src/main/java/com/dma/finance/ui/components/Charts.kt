package com.dma.finance.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dma.finance.data.local.relation.CategorySpending
import com.dma.finance.data.local.relation.MonthlySummary
import com.dma.finance.ui.theme.ExpenseRed
import com.dma.finance.ui.theme.IncomeGreen
import com.dma.finance.util.CurrencyFormatter
import com.dma.finance.util.DateUtils

/** Graphique en barres groupées (revenus / dépenses) mois par mois. */
@Composable
fun MonthlyBarChart(data: List<MonthlySummary>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val maxValue = data.maxOf { maxOf(it.incomeMinor, it.expenseMinor) }.coerceAtLeast(1L)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { month ->
                Row(
                    modifier = Modifier
                        .height(160.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Bar(
                        heightFraction = month.incomeMinor.toFloat() / maxValue.toFloat(),
                        color = IncomeGreen
                    )
                    Bar(
                        heightFraction = month.expenseMinor.toFloat() / maxValue.toFloat(),
                        color = ExpenseRed
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            data.forEach { month ->
                Text(
                    text = DateUtils.formatYearMonth(month.yearMonth),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun Bar(heightFraction: Float, color: Color) {
    val clamped = heightFraction.coerceIn(0f, 1f)
    Canvas(
        modifier = Modifier
            .size(width = 14.dp, height = 140.dp)
    ) {
        val barHeight = size.height * clamped
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - barHeight),
            size = Size(size.width, barHeight),
            style = Fill
        )
    }
}

/** Répartition des dépenses par catégorie, sous forme de barres horizontales proportionnelles. */
@Composable
fun CategoryBreakdownList(data: List<CategorySpending>, currencyCode: String, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val total = data.sumOf { it.totalMinor }.coerceAtLeast(1L)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        data.forEach { category ->
            val fraction = category.totalMinor.toFloat() / total.toFloat()
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = category.categoryName, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = CurrencyFormatter.format(category.totalMinor, currencyCode),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.size(4.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(MaterialTheme.shapes.small)
                ) {
                    drawTrackAndBar(fraction, parseColor(category.colorHex))
                }
            }
        }
    }
}

private fun DrawScope.drawTrackAndBar(fraction: Float, color: Color) {
    drawRect(color = color.copy(alpha = 0.15f), size = size)
    drawRect(color = color, size = Size(size.width * fraction.coerceIn(0f, 1f), size.height))
}
