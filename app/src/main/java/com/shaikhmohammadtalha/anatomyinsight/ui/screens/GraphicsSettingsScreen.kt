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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.shaikhmohammadtalha.anatomyinsight.datastore.AppTheme
import com.shaikhmohammadtalha.anatomyinsight.datastore.GraphicsSettings
import com.shaikhmohammadtalha.anatomyinsight.datastore.GraphicsSettingsStore
import com.shaikhmohammadtalha.anatomyinsight.ui.theme.AnatomyInsightTheme
import kotlinx.coroutines.launch

/**
 * Composable screen for configuring graphics settings including quality presets and rendering effects.
 * Stores user preferences using a local data store and updates them on selection.
 *
 * @param navController Used for back navigation
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GraphicsSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // New local state for user-defined search result limit
    var searchResultLimit by remember { mutableStateOf("20") }

    // Observe stored settings from the persistent data store
    val settings by GraphicsSettingsStore.settingsFlow(context)
        .collectAsState(initial = GraphicsSettings("Medium", true, true, true, 20))

    // Local state to track UI selections
    var selectedQuality by remember { mutableStateOf(settings.quality) }
    var msaaEnabled by remember { mutableStateOf(settings.msaa) }
    var aoEnabled by remember { mutableStateOf(settings.ao) }
    var bloomEnabled by remember { mutableStateOf(settings.bloom) }

    val qualities = listOf("LOW", "MEDIUM", "HIGH", "ULTRA")

    // Keep state synced with persisted store
    LaunchedEffect(Unit) {
        GraphicsSettingsStore.settingsFlow(context).collect { settings ->
            selectedQuality = settings.quality
            msaaEnabled = settings.msaa
            aoEnabled = settings.ao
            bloomEnabled = settings.bloom
            searchResultLimit = settings.searchLimit.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Graphics Settings") },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section: Render Quality Options
            Text("Render Quality", style = MaterialTheme.typography.titleMedium)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                qualities.forEach { quality ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            selectedQuality = quality
                            scope.launch {
                                GraphicsSettingsStore.saveSettings(
                                    context,
                                    selectedQuality,
                                    msaaEnabled,
                                    aoEnabled,
                                    bloomEnabled,
                                    searchResultLimit.toInt()
                                )
                            }
                        }
                    ) {
                        RadioButton(
                            selected = selectedQuality == quality,
                            onClick = {
                                selectedQuality = quality
                                scope.launch {
                                    GraphicsSettingsStore.saveSettings(
                                        context,
                                        selectedQuality,
                                        msaaEnabled,
                                        aoEnabled,
                                        bloomEnabled,
                                        searchResultLimit.toInt()
                                    )
                                }
                            }
                        )
                        Text(text = quality, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                thickness = 1.dp
            )

            // Section: Toggleable Graphics Effects
            Text("Additional Effects", style = MaterialTheme.typography.titleMedium)

            SettingSwitch("Multi-sample Anti-Aliasing (MSAA)", msaaEnabled) {
                msaaEnabled = it
                scope.launch {
                    GraphicsSettingsStore.saveSettings(
                        context, selectedQuality, msaaEnabled, aoEnabled, bloomEnabled, searchResultLimit.toInt()
                    )
                }
            }

            SettingSwitch("Ambient Occlusion", aoEnabled) {
                aoEnabled = it
                scope.launch {
                    GraphicsSettingsStore.saveSettings(
                        context, selectedQuality, msaaEnabled, aoEnabled, bloomEnabled, searchResultLimit.toInt()
                    )
                }
            }

            SettingSwitch("Bloom", bloomEnabled) {
                bloomEnabled = it
                scope.launch {
                    GraphicsSettingsStore.saveSettings(
                        context, selectedQuality, msaaEnabled, aoEnabled, bloomEnabled, searchResultLimit.toInt()
                    )
                }
            }

            // Additional input section: Dynamic Search Result Limit
            Text("Search Result Limit", style = MaterialTheme.typography.titleMedium)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = searchResultLimit,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            searchResultLimit = newValue
                            newValue.toIntOrNull()?.let { value ->
                                if (value in 10..100) {
                                    scope.launch {
                                        GraphicsSettingsStore.saveSettings(
                                            context,
                                            selectedQuality,
                                            msaaEnabled,
                                            aoEnabled,
                                            bloomEnabled,
                                            value
                                        )
                                    }
                                }
                            }
                        }
                    },
                    label = { Text("Max Results (10 - 100)") },
                    placeholder = { Text("Enter a value between 10 and 100") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Controls how many top-matching subparts appear in search results. Lower values improve performance on older devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Reusable switch component for enabling/disabling a single graphics feature.
 *
 * @param label Label displayed alongside the switch
 * @param isChecked Current checked state
 * @param onToggle Callback to handle toggle change
 */
@Composable
fun SettingSwitch(label: String, isChecked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = isChecked, onCheckedChange = onToggle)
    }
}

@Preview(showBackground = true)
@Composable
fun GraphicsSettingsScreenPreview() {
    // Mock NavController for preview purposes
    val mockNavController = rememberNavController()

    // Wrap the screen in a theme to preview it with material styling
    AnatomyInsightTheme(appTheme = AppTheme.LIGHT) {
        GraphicsSettingsScreen(navController = mockNavController)
    }
}
