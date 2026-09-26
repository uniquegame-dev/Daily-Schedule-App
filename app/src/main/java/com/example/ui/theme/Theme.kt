package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private val CleanLightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerLightBlue,
    onPrimaryContainer = OnPrimaryContainerBlue,
    secondary = SecondaryEmerald,
    secondaryContainer = SecondaryContainerEmerald,
    background = BackgroundLight,
    onBackground = OnBackgroundDark,
    surface = SurfaceWhite,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextMuted,
    outline = BorderLight
)

private val CleanDarkColorScheme = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = Color(0xFF172554),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF6EE7B7),
    secondaryContainer = Color(0xFF064E3B),
    background = BackgroundDark,
    onBackground = OnBackgroundLight,
    surface = SurfaceDark,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextMutedDark,
    outline = BorderDark
)

@Composable
fun DailyScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) CleanDarkColorScheme else CleanLightColorScheme,
        typography = Typography,
        content = content
    )
}
