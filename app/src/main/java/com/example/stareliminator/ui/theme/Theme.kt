package com.example.stareliminator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGold,
    secondary = AccentBlue,
    tertiary = AccentRed,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = BackgroundDark,
    onSecondary = TextWhite,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun StarEliminatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
