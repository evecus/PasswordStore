package com.mobile.passwordmanager.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Indigo500,
    onPrimary = Color.White,
    primaryContainer = Indigo100,
    onPrimaryContainer = Indigo900,
    secondary = Violet500,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAD9FF),
    onSecondaryContainer = Color(0xFF3A0060),
    tertiary = Pink400,
    onTertiary = Color.White,
    error = Color(0xFFB3261E),
    onError = Color.White,
    background = Color(0xFFFBFAFF),
    onBackground = Color(0xFF1B1A26),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1A26),
    surfaceVariant = Color(0xFFF1EFFA),
    onSurfaceVariant = Color(0xFF5A576B),
    outline = OutlineSoft,
    outlineVariant = Color(0xFFEDEBF5)
)

private val DarkColors = darkColorScheme(
    primary = Indigo300,
    onPrimary = Indigo900,
    primaryContainer = Indigo800,
    onPrimaryContainer = Indigo100,
    secondary = Violet400,
    onSecondary = Color(0xFF25003F),
    secondaryContainer = Color(0xFF4B1477),
    onSecondaryContainer = Color(0xFFEAD9FF),
    tertiary = Pink400,
    onTertiary = Color(0xFF3F0018),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    background = Color(0xFF12111A),
    onBackground = Color(0xFFE7E5EF),
    surface = Color(0xFF1B1A26),
    onSurface = Color(0xFFE7E5EF),
    surfaceVariant = Color(0xFF2A2836),
    onSurfaceVariant = Color(0xFFC3C0D2),
    outline = Color(0xFF3A3848),
    outlineVariant = Color(0xFF2A2836)
)

@Composable
fun PasswordManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
