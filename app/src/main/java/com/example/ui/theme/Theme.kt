package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val YouTubeDarkColorScheme = darkColorScheme(
    primary = YtRed,
    onPrimary = Color.White,
    primaryContainer = YtSurfaceVariant,
    onPrimaryContainer = YtTextPrimary,
    secondary = YtBlue,
    onSecondary = Color.White,
    secondaryContainer = YtSurfaceVariant,
    onSecondaryContainer = YtTextPrimary,
    background = YtDarkBackground,
    onBackground = YtTextPrimary,
    surface = YtDarkBackground,
    onSurface = YtTextPrimary,
    surfaceVariant = YtSurfaceVariant,
    onSurfaceVariant = YtTextSecondary,
    outline = YtBorder,
    outlineVariant = YtSurfaceHigher
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force modern YouTube Pure Dark Theme
    dynamicColor: Boolean = false, // Keep authentic YouTube branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = YouTubeDarkColorScheme,
        typography = Typography,
        content = content
    )
}
