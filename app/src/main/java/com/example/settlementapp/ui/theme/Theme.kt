package com.example.settlementapp.ui.theme

import android.app.Activity
import android.os.Build
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

private val LightColors = lightColorScheme(
    primary = Teal700,
    onPrimary = Color.White,
    primaryContainer = Teal100,
    onPrimaryContainer = Teal700,
    secondary = GoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF1C2),
    onSecondaryContainer = Color(0xFF5A4300),
    tertiary = Teal500,
    background = CloudBg,
    onBackground = Ink,
    surface = SurfaceCard,
    onSurface = Ink,
    surfaceVariant = Teal50,
    onSurfaceVariant = Slate,
    error = DangerRed,
    onError = Color.White,
    outline = Color(0xFFC4D3CF)
)

private val DarkColors = darkColorScheme(
    primary = Teal500,
    onPrimary = Color.White,
    primaryContainer = TealDark,
    onPrimaryContainer = Teal100,
    secondary = Gold,
    onSecondary = Color(0xFF3A2C00),
    background = DarkBg,
    onBackground = Color(0xFFE7EDEB),
    surface = DarkSurface,
    onSurface = Color(0xFFE7EDEB),
    surfaceVariant = Color(0xFF22302D),
    onSurfaceVariant = Color(0xFFAFC0BB),
    error = DangerRed,
    onError = Color.White,
    outline = Color(0xFF3C4B47)
)

@Composable
fun SettlementAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
