package com.dma.finance.util

import com.dma.finance.data.local.entity.BudgetPeriod
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/** Calcule les bornes temporelles d'une période budgétaire à partir de sa date de début. */
object PeriodUtils {

    fun periodEndMillis(startMillis: Long, period: BudgetPeriod): Long {
        val start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMillis), ZoneId.systemDefault())
        val end = when (period) {
            BudgetPeriod.WEEKLY -> start.plusWeeks(1)
            BudgetPeriod.MONTHLY -> start.plusMonths(1)
            BudgetPeriod.YEARLY -> start.plusYears(1)
        }
        return end.toInstant().toEpochMilli()
    }

    /** Début et fin (millis) du mois civil contenant [atMillis]. */
    fun currentMonthRange(atMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(atMillis), zone)
        val start = date.withDayOfMonth(1).toLocalDate().atStartOfDay(zone)
        val end = start.plusMonths(1)
        return start.toInstant().toEpochMilli() to end.toInstant().toEpochMilli()
    }

    /** Début et fin (millis) des N derniers mois (inclus le mois courant), pour les rapports. */
    fun lastMonthsRange(monthsCount: Int, atMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(atMillis), zone)
        val end = date.withDayOfMonth(1).toLocalDate().atStartOfDay(zone).plusMonths(1)
        val start = end.minusMonths(monthsCount.toLong())
        return start.toInstant().toEpochMilli() to end.toInstant().toEpochMilli()
    }
}
