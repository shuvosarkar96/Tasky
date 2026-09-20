package com.tasky.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Light = lightColorScheme(
    primary = Color(0xFF4F46E5), onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E0FF), secondary = Color(0xFF0D9488),
    tertiary = Color(0xFFF59E0B), background = Color(0xFFFBF8FF), surface = Color(0xFFFBF8FF)
)
private val Dark = darkColorScheme(
    primary = Color(0xFFBEC2FF), onPrimary = Color(0xFF1E1B6E),
    primaryContainer = Color(0xFF3730A3), secondary = Color(0xFF5EEAD4),
    tertiary = Color(0xFFFBBF24), background = Color(0xFF121218), surface = Color(0xFF121218)
)

@Composable
fun TaskyTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= 31 -> if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> Dark
        else -> Light
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
