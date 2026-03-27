package com.example.wifiinspectorpro.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light Mode Palette (Keeping the "Nocturne" structure but with light surfaces)
private val LightColorScheme = lightColorScheme(
    primary = GradientStart,
    secondary = PulseCoral,
    background = Color(0xFFFBFBFF), // Soft light background
    surface = Color(0xFFFFFFFF),    // Pure white cards
    onBackground = Color(0xFF14121B),
    onSurface = Color(0xFF14121B)
)

private val DarkColorScheme = darkColorScheme(
    primary = KineticLavender,
    secondary = PulseCoral,
    background = ObsidianBg,
    surface = SurfaceLow,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun WiFiInspectorProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Automatically detects system setting
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}