package com.dma.finance.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val displayFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH)
    private val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH)

    fun formatDate(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(displayFormatter)

    fun formatYearMonth(yearMonth: String): String {
        val parts = yearMonth.split("-")
        if (parts.size != 2) return yearMonth
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val date = java.time.LocalDate.of(year, month, 1)
        return date.format(monthFormatter)
    }
}
