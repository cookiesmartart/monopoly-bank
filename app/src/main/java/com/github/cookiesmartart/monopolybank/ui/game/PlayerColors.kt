package com.github.cookiesmartart.monopolybank.ui.game

import androidx.compose.ui.graphics.Color

/** The classic Monopoly board property-group colors, used as player picks in the setup screen. */
val PlayerColorPalette: List<String> = listOf(
    "#8B4513", // brown
    "#87CEEB", // light blue
    "#D6006D", // pink/magenta
    "#FF8C00", // orange
    "#E30022", // red
    "#FFD700", // yellow
    "#1B5E20", // green
    "#00008B" // dark blue
)

fun String.toColor(): Color = Color(android.graphics.Color.parseColor(this))
