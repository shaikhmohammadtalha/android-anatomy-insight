package com.shaikhmohammadtalha.anatomyinsight.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.shaikhmohammadtalha.anatomyinsight.datastore.AppTheme
// Predefined dark color scheme using custom color constants
private val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    onPrimary = WhiteText,
    primaryContainer = DarkGrayBackground,
    secondary = JetBlack,
    onSecondary = WhiteText,
    tertiary = WhiteText,
    onTertiary = DarkGrayBackground,
    background = DarkGrayBackground,
    surface = CharcoalSurface,
    onSurface = WhiteText,
    outline = TealOutline
)

// Predefined light color scheme using custom color constants
private val LightColorScheme = lightColorScheme(
    primary = RedPrimary,
    onPrimary = WhiteText,
    primaryContainer = LightGraySurface,
    secondary = LightGrayBackground,
    onSecondary = DarkText,
    tertiary = WhiteText,
    onTertiary = DarkGrayBackground,
    background = LightGrayBackground,
    surface = LightGraySurface,
    onSurface = DarkText,
    outline = TealOutline
)

@Composable
fun AnatomyInsightTheme(
    appTheme: AppTheme, // Custom theme mode: AUTO, LIGHT, DARK, or DYNAMIC
    content: @Composable () -> Unit // Slot for UI content to be wrapped in the theme
) {
    val context = LocalContext.current

    // Resolve the theme dynamically based on the appTheme and system settings
    val resolvedTheme = when (appTheme) {
        AppTheme.AUTO -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AppTheme.DYNAMIC // Use dynamic theming on supported versions
            } else if (!isSystemInDarkTheme()) {
                AppTheme.LIGHT // Fallback to light theme
            } else {
                AppTheme.DARK // Fallback to dark theme
            }
        }
        else -> appTheme // Use explicitly set theme
    }

    // Determine if the dark theme should be applied
    val darkTheme = when (resolvedTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.DYNAMIC -> isSystemInDarkTheme() // Follow system dark theme setting
        else -> isSystemInDarkTheme() // Fallback for unknown cases
    }

    // Select appropriate color scheme based on resolved theme and Android version
    val colorScheme = when {
        resolvedTheme == AppTheme.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply the selected theme configuration to MaterialTheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Apply custom typography
        content = content // Render UI content within the theme
    )
}
