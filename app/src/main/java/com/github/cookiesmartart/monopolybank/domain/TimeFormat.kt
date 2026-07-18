package com.github.cookiesmartart.monopolybank.domain

import java.text.DateFormat
import java.util.Date
import java.util.Locale

fun formatTime(timestampMillis: Long, locale: Locale = Locale.getDefault()): String =
    DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(Date(timestampMillis))
