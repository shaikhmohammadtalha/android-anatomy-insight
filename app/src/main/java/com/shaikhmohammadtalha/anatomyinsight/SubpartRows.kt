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
package com.shaikhmohammadtalha.anatomyinsight

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shaikhmohammadtalha.anatomyinsight.ui.theme.AnatomyInsightTheme

@Composable
fun SubpartRows(
    subparts: List<AnatomyModel>,
    onSubpartSelect: (AnatomyModel) -> Unit,
    currentModel: AnatomyModel?,
    selectedSubpart: AnatomyModel?, // 🔹 Receive the selected subpart from parent
    showMainModels: Boolean,
    toggleMainModels: () -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    var showDescription by remember { mutableStateOf(false) } // Toggles description visibility

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            if (showDescription) {
                // 🔹 Show Subpart Description
                item {
                    ExpandableCard(
                        title = selectedSubpart?.name ?: (currentModel?.name ?: "Unknown"),
                        description = "${selectedSubpart?.name ?: (currentModel?.name ?: "Unknown")} is a detailed anatomical subpart. It enhances understanding through high-quality 3D visualization."
                    )
                }
            } else {
                // 🔹 Show Subpart List
                items(subparts) { subpart ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                            .clickable { onSubpartSelect(subpart) }, // Now updates globally
                        shadowElevation = 4.dp,
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 🔹 Left: Icon Button
                            IconButton(onClick = { onSubpartSelect(subpart) }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Subpart Icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // 🔹 Middle: Subpart Name
                            Text(
                                text = subpart.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            // 🔹 Right: Empty space for future buttons (D & M)
                            Spacer(modifier = Modifier.width(50.dp))
                        }
                    }
                }
            }
        }

        if (currentModel != null) {
            // 🔹 Button to toggle between Models & Subparts
            Button(
                onClick = { toggleMainModels() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (showMainModels) "View Subparts" else "Back to Models",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // 🔹 Floating Button - Toggles between Subpart Description & List
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val subpartRowsHeight = screenHeight * 0.40f
        val buttonSize = subpartRowsHeight * 0.3f
        if (currentModel != null) {
            FloatingActionButton(
                onClick = { showDescription = !showDescription }, // Toggle Description
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(buttonSize)
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = if (showDescription) "M" else "D", // "M" for Model view, "D" for Description view
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SubpartRowsPreview() {
    val sampleSubparts = listOf(
        AnatomyModel("Heart", "models/heart.glb"),
        AnatomyModel("Lungs", "models/lungs.glb"),
        AnatomyModel("Liver", "models/liver.glb"),
        AnatomyModel("Kidney", "models/kidney.glb")
    )

    var currentModel by remember { mutableStateOf(sampleSubparts.first()) } // Default to first model
    var selectedSubpart by remember { mutableStateOf<AnatomyModel?>(null) } // Track selected subpart
    var showMainModels by remember { mutableStateOf(false) }

    AnatomyInsightTheme(darkTheme = true) {
        Surface {
            SubpartRows(
                subparts = sampleSubparts,
                onSubpartSelect = { selectedSubpart = it; currentModel = it }, // Update both states
                currentModel = currentModel,
                selectedSubpart = selectedSubpart, // Pass selected subpart
                showMainModels = showMainModels,
                toggleMainModels = { showMainModels = !showMainModels }
            )
        }
    }
}


