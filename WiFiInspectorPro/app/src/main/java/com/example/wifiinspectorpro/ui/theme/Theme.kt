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

// Light Mode Palette (Modern Minimalist Alabaster Slate)
private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimaryLight,
    secondary = IndigoSecondaryLight,
    background = SlateBgLight,
    surface = SurfaceLowLight,
    surfaceVariant = SurfaceBrightLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight
)

// Dark Mode Palette (Modern Minimalist Slate Black)
private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryDark,
    secondary = IndigoSecondaryDark,
    background = SlateBgDark,
    surface = SurfaceLowDark,
    surfaceVariant = SurfaceBrightDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark
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
            window.navigationBarColor = colorScheme.background.toArgb()
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}