package com.github.cookiesmartart.monopolybank.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = MonopolyGreen,
    onPrimary = Color.White,
    primaryContainer = MonopolyGreenPale,
    onPrimaryContainer = MonopolyGreenDeep,
    secondary = MonopolyGold,
    onSecondary = Color.White,
    secondaryContainer = MonopolyGoldPale,
    onSecondaryContainer = Color(0xFF4A3300),
    tertiary = MonopolyBlue,
    onTertiary = Color.White,
    tertiaryContainer = MonopolyBluePale,
    onTertiaryContainer = Color(0xFF0E2E5C),
    error = MonopolyRed,
    onError = Color.White,
    errorContainer = MonopolyRedPale,
    onErrorContainer = Color(0xFF5C140C),
    background = PaperLight,
    onBackground = PaperLightOnSurface,
    surface = PaperLightSurface,
    onSurface = PaperLightOnSurface,
    surfaceVariant = PaperLightVariant,
    onSurfaceVariant = PaperLightOnSurfaceMuted,
    outline = PaperLightOutline
)

private val DarkColors = darkColorScheme(
    primary = MonopolyGreenBright,
    onPrimary = MonopolyGreenDeep,
    primaryContainer = MonopolyGreenDeep,
    onPrimaryContainer = MonopolyGreenPale,
    secondary = MonopolyGoldBright,
    onSecondary = Color(0xFF3D2900),
    secondaryContainer = Color(0xFF5A3D00),
    onSecondaryContainer = MonopolyGoldPale,
    tertiary = MonopolyBlueBright,
    onTertiary = Color(0xFF0E2E5C),
    tertiaryContainer = Color(0xFF1E3E6E),
    onTertiaryContainer = MonopolyBluePale,
    error = MonopolyRedBright,
    onError = Color(0xFF5C140C),
    errorContainer = Color(0xFF7A2015),
    onErrorContainer = MonopolyRedPale,
    background = TableDark,
    onBackground = TableDarkOnSurface,
    surface = TableDarkSurface,
    onSurface = TableDarkOnSurface,
    surfaceVariant = TableDarkVariant,
    onSurfaceVariant = TableDarkOnSurfaceMuted,
    outline = TableDarkOutline
)

@Composable
fun MonopolyBankTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MonopolyTypography,
        shapes = MonopolyShapes,
        content = content
    )
}
