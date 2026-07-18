package com.github.cookiesmartart.monopolybank

sealed interface Screen {
    data object MainMenu : Screen
    data class Setup(val nfcMode: Boolean) : Screen
    data object LinkTags : Screen
    data object Home : Screen
    data object History : Screen
    data object Settings : Screen
    data object About : Screen
}
