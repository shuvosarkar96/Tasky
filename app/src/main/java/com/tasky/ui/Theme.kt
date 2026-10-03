package com.tasky.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    tertiary = Color(0xFFF59E0B),
    onTertiary = Color.White,
    background = Color(0xFFF8F8FC),
    onBackground = Color(0xFF1A1A20),
    surface = Color(0xFFF8F8FC),
    onSurface = Color(0xFF1A1A20),
    surfaceVariant = Color(0xFFE8E8F0),
    onSurfaceVariant = Color(0xFF5F606B)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB8B9FF),
    onPrimary = Color(0xFF25236B),
    primaryContainer = Color(0xFF3B388E),
    onPrimaryContainer = Color(0xFFE1E0FF),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF003731),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF3D2E00),
    background = Color(0xFF121218),
    onBackground = Color(0xFFE5E1E9),
    surface = Color(0xFF121218),
    onSurface = Color(0xFFE5E1E9),
    surfaceVariant = Color(0xFF46464F),
    onSurfaceVariant = Color(0xFFC7C5D0)
)

@Composable
fun TaskyTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}