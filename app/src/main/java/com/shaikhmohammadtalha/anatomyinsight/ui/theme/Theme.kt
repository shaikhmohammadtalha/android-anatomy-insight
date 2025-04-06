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

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = Background,
    surface = Surface,
    onSurface = OnSurface,
    outline = Outline
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Secondary,
    primaryContainer = LightSurface,
    secondary = LightBackground,
    onSecondary = OnPrimary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = LightBackground,
    surface = LightSurface,
    onSurface = OnLightSurface,
    outline = Outline
)

@Composable
fun AnatomyInsightTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
