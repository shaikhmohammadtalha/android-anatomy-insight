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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shaikhmohammadtalha.anatomyinsight.datastore.GraphicsSettings
import com.shaikhmohammadtalha.anatomyinsight.datastore.GraphicsSettingsStore
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Graphics settings screen with segmented quality selector, effect toggles, and search limit slider.
 * Single Scaffold with one TopAppBar and Apply button pinned to the bottom.
 *
 * @param navController Used for back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphicsSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val settings by GraphicsSettingsStore.settingsFlow(context)
        .collectAsState(initial = GraphicsSettings("LOW", true, true, true, 20))

    var selectedQuality by remember { mutableStateOf("LOW") }
    var msaaEnabled by remember { mutableStateOf(true) }
    var aoEnabled by remember { mutableStateOf(true) }
    var bloomEnabled by remember { mutableStateOf(true) }
    var searchLimit by remember { mutableFloatStateOf(20f) }

    // Sync local state from persisted settings once
    LaunchedEffect(Unit) {
        GraphicsSettingsStore.settingsFlow(context).collect { s ->
            selectedQuality = s.quality
            msaaEnabled = s.msaa
            aoEnabled = s.ao
            bloomEnabled = s.bloom
            searchLimit = s.searchLimit.toFloat()
        }
    }

    val qualityOptions = listOf("LOW", "MEDIUM", "HIGH", "ULTRA")
    val qualityDescriptions = mapOf(
        "LOW" to "Best performance. Suitable for older devices.",
        "MEDIUM" to "Balanced quality and performance.",
        "HIGH" to "High fidelity. Requires a capable GPU.",
        "ULTRA" to "Maximum quality. May impact battery life."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Graphics Quality") },
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
        },
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            GraphicsSettingsStore.saveSettings(
                                context, selectedQuality, msaaEnabled, aoEnabled, bloomEnabled,
                                searchLimit.roundToInt()
                            )
                        }
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                        .padding(16.dp)
                ) {
                    Text("Apply Settings")
                }
            }
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

            // ── Render Quality ───────────────────────────────────────────────
            SettingsSectionHeader("Render Quality")

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                qualityOptions.forEachIndexed { index, quality ->
                    SegmentedButton(
                        selected = selectedQuality == quality,
                        onClick = { selectedQuality = quality },
                        shape = SegmentedButtonDefaults.itemShape(index, qualityOptions.size),
                        label = {
                            Text(
                                quality.replaceFirstChar { it.uppercase() }.take(3),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = qualityDescriptions[selectedQuality] ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            // ── Effects ─────────────────────────────────────────────────────
            SettingsSectionHeader("Effects")

            ListItem(
                headlineContent = { Text("Multi-Sample Anti-Aliasing") },
                supportingContent = { Text("Smooths jagged edges on model surfaces") },
                trailingContent = {
                    Switch(
                        checked = msaaEnabled,
                        onCheckedChange = { msaaEnabled = it }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Ambient Occlusion") },
                supportingContent = { Text("Adds depth shading to crevices") },
                trailingContent = {
                    Switch(
                        checked = aoEnabled,
                        onCheckedChange = { aoEnabled = it }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Bloom") },
                supportingContent = { Text("Adds a glow effect to bright areas") },
                trailingContent = {
                    Switch(
                        checked = bloomEnabled,
                        onCheckedChange = { bloomEnabled = it }
                    )
                }
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            // ── Search Result Limit ──────────────────────────────────────────
            SettingsSectionHeader("Search")

            Text(
                text = "Max results: ${searchLimit.roundToInt()}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Slider(
                value = searchLimit,
                onValueChange = { searchLimit = it },
                valueRange = 5f..100f,
                steps = 18,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text(
                text = "Controls how many matching subparts appear in search results.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(80.dp)) // space for bottomBar
        }
    }
}
}
