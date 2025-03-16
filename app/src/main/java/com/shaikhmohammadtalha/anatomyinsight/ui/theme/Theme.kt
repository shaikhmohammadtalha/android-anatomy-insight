package com.shaikhmohammadtalha.anatomyinsight.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue500,           // Keeping blue as primary color for consistency
    onPrimary = Color.White,
    primaryContainer = Blue200,  // A lighter blue for primary container
    secondary = Green200,        // Keeping green for secondary color
    onSecondary = Color.Black,
    background = Color(0xFF121212), // Darker background for dark theme
    surface = Color(0xFF1F1F1F),   // Slightly lighter dark surface
    onSurface = Color.White      // White text on dark surface
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),    // A vibrant purple for light theme primary
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBB86FC), // Lighter purple for primary container
    secondary = Color(0xFF03DAC6), // Teal for the secondary color
    onSecondary = Color.Black,
    background = Color(0xFFF5F5F5), // Lighter grey background for light theme
    surface = Color.White,         // White surface for a clean light theme
    onSurface = Color.Black        // Black text on light surface

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun AnatomyInsightTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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