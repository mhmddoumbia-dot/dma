package com.dma.finance.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Formate les montants stockés en unité mineure (ex: centimes) vers une chaîne lisible.
 * Le XOF (Franc CFA) n'a pas de sous-unité utilisée en pratique : on l'affiche donc
 * comme un entier, tout en conservant amountMinor en base 100 pour l'uniformité du modèle.
 */
object CurrencyFormatter {

    private val zeroDecimalCurrencies = setOf("XOF", "XAF", "JPY", "KRW")

    fun format(amountMinor: Long, currencyCode: String): String {
        val locale = Locale.FRANCE
        val format = NumberFormat.getNumberInstance(locale)
        return if (currencyCode in zeroDecimalCurrencies) {
            format.maximumFractionDigits = 0
            "${format.format(amountMinor / 100)} $currencyCode"
        } else {
            format.minimumFractionDigits = 2
            format.maximumFractionDigits = 2
            "${format.format(amountMinor / 100.0)} $currencyCode"
        }
    }

    /** Convertit une saisie utilisateur (ex: "12500,50" ou "12500.50") en unité mineure. */
    fun parseToMinor(input: String): Long? {
        val normalized = input.trim().replace(",", ".")
        val value = normalized.toDoubleOrNull() ?: return null
        return Math.round(value * 100)
    }
}
