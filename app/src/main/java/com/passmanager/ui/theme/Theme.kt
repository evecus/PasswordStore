package com.passmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF1A1A2E)
val PrimaryVariant = Color(0xFF16213E)
val Secondary = Color(0xFF6366F1)
val SecondaryLight = Color(0xFFEEF2FF)
val Background = Color(0xFFFFFFFF)
val Surface = Color(0xFFF8F9FA)
val SurfaceVariant = Color(0xFFF1F3F9)
val Error = Color(0xFFEF4444)
val OnPrimary = Color(0xFFFFFFFF)
val OnBackground = Color(0xFF1A1A2E)
val OnSurface = Color(0xFF374151)
val OnSurfaceVariant = Color(0xFF6B7280)
val Divider = Color(0xFFE5E7EB)
val Success = Color(0xFF10B981)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    error = Error,
    onPrimary = OnPrimary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    onSecondary = OnPrimary,
    outline = Divider
)

@Composable
fun PasswordManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}

val groupColors = listOf(
    Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899),
    Color(0xFFEF4444), Color(0xFFF97316), Color(0xFFF59E0B),
    Color(0xFF10B981), Color(0xFF14B8A6), Color(0xFF3B82F6),
    Color(0xFF06B6D4)
)
