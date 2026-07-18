package com.github.cookiesmartart.monopolybank.data.settings

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val nfcEnabled: Boolean = true,
    val defaultStartingBalance: Long = 1500L
)
