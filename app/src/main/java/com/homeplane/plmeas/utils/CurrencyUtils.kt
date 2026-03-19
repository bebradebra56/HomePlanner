package com.homeplane.plmeas.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun format(amount: Double, currency: String): String = try {
        NumberFormat.getCurrencyInstance(localeFor(currency)).format(amount)
    } catch (e: Exception) {
        "${symbol(currency)}${String.format("%.2f", amount)}"
    }

    fun formatShort(amount: Double, currency: String): String {
        val sym = symbol(currency)
        return when {
            amount >= 1_000_000 -> "$sym${String.format("%.1f", amount / 1_000_000)}M"
            amount >= 1_000 -> "$sym${String.format("%.1f", amount / 1_000)}k"
            else -> "$sym${amount.toLong()}"
        }
    }

    fun symbol(currency: String): String = when (currency.uppercase()) {
        "EUR" -> "€"
        "GBP" -> "£"
        "RUB" -> "₽"
        "JPY" -> "¥"
        "CNY" -> "¥"
        else -> "$"
    }

    fun localeFor(currency: String): Locale = when (currency.uppercase()) {
        "EUR" -> Locale.GERMANY
        "GBP" -> Locale.UK
        "RUB" -> Locale("ru", "RU")
        "JPY" -> Locale.JAPAN
        "CNY" -> Locale.CHINA
        else -> Locale.US
    }
}
