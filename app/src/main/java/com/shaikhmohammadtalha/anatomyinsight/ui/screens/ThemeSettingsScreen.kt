/*
 * Copyright 2025 Shaikh Mohammad Talha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shaikhmohammadtalha.anatomyinsight.ui.screens

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shaikhmohammadtalha.anatomyinsight.datastore.AppTheme
import com.shaikhmohammadtalha.anatomyinsight.datastore.ThemeStore
import kotlinx.coroutines.launch

/**
 * Appearance/theme settings screen using a segmented button row for theme selection.
 * Shows an explanation card for the currently selected theme.
 *
 * @param navController Used for back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val themes = listOf(AppTheme.AUTO, AppTheme.LIGHT, AppTheme.DARK, AppTheme.DYNAMIC)
    val currentTheme by ThemeStore.themeFlow(context).collectAsState(initial = AppTheme.AUTO)

    // Resolve what AUTO would actually use on this device
    val resolvedTheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> AppTheme.DYNAMIC
        isSystemInDarkTheme() -> AppTheme.DARK
        else -> AppTheme.LIGHT
    }

    val themeIcon = @Composable { theme: AppTheme ->
        when (theme) {
            AppTheme.AUTO -> Icon(Icons.Default.AutoMode, contentDescription = null, modifier = Modifier.size(18.dp))
            AppTheme.LIGHT -> Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(18.dp))
            AppTheme.DARK -> Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(18.dp))
            AppTheme.DYNAMIC -> Icon(Icons.Default.BrightnessMedium, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }

    val themeLabel: (AppTheme) -> String = { theme ->
        when (theme) {
            AppTheme.AUTO -> "Auto"
            AppTheme.LIGHT -> "Light"
            AppTheme.DARK -> "Dark"
            AppTheme.DYNAMIC -> "Dynamic"
        }
    }

    val themeDescription: (AppTheme) -> String = { theme ->
        when (theme) {
            AppTheme.AUTO -> "Follows your device's dark/light system setting. Currently resolves to: ${resolvedTheme.name.lowercase().replaceFirstChar { it.uppercase() }}."
            AppTheme.LIGHT -> "Forces light mode regardless of system setting."
            AppTheme.DARK -> "Forces dark mode regardless of system setting."
            AppTheme.DYNAMIC -> "Uses Material You dynamic colors from your wallpaper. Requires Android 12 (API 31) or higher."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                windowInsets = TopAppBarDefaults.windowInsets
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Theme", style = MaterialTheme.typography.titleMedium)

                // Segmented button row for all 4 theme options
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    themes.forEachIndexed { index, theme ->
                        SegmentedButton(
                            selected = currentTheme == theme,
                            onClick = { scope.launch { ThemeStore.saveTheme(context, theme) } },
                            shape = SegmentedButtonDefaults.itemShape(index, themes.size),
                            icon = { themeIcon(theme) },
                            label = { Text(themeLabel(theme), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Description card for the currently selected theme
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = themeDescription(currentTheme),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
