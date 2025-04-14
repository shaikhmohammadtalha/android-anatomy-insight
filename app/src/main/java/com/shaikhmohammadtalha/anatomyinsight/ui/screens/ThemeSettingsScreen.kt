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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
 * Composable screen for selecting the application theme.
 * Supports Light, Dark, Auto (follow system), and Dynamic (Material You, API 31+).
 *
 * @param navController Used for back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Available theme options for selection
    val themes = listOf(AppTheme.AUTO, AppTheme.LIGHT, AppTheme.DARK, AppTheme.DYNAMIC)

    // Currently active theme from the datastore
    val currentTheme by ThemeStore.themeFlow(context).collectAsState(initial = AppTheme.AUTO)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Select App Theme", style = MaterialTheme.typography.titleMedium)

            // Resolve which theme would be used under AUTO mode
            val resolvedTheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> AppTheme.DYNAMIC
                isSystemInDarkTheme() -> AppTheme.DARK
                else -> AppTheme.LIGHT
            }

            // Display radio buttons for each theme option
            themes.forEach { themeOption ->
                val selected = currentTheme == themeOption

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                ThemeStore.saveTheme(context, themeOption)
                            }
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = {
                                scope.launch {
                                    ThemeStore.saveTheme(context, themeOption)
                                }
                            }
                        )
                        Text(
                            text = themeOption.name.lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Additional context for AUTO mode
                    if (themeOption == AppTheme.AUTO && selected) {
                        Text(
                            text = "→ Currently using: ${
                                resolvedTheme.name.lowercase().replaceFirstChar { it.uppercase() }
                            } theme",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 48.dp) // indent under radio
                        )
                    }
                }
            }
        }
    }
}
