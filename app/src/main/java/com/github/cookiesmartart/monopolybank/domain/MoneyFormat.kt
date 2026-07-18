package com.github.cookiesmartart.monopolybank.domain

import java.text.NumberFormat
import java.util.Locale

fun formatMoney(amount: Long, locale: Locale = Locale.getDefault()): String {
    val format = NumberFormat.getIntegerInstance(locale)
    return "€${format.format(amount)}"
}
