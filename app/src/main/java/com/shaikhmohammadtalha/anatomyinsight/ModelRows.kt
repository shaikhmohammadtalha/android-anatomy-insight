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

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.shaikhmohammadtalha.anatomyinsight.ui.theme.AnatomyInsightTheme

@Composable
fun ModelRows(
    models: List<AnatomyModel>,
    onModelSelect: (AnatomyModel) -> Unit,
    currentModel: AnatomyModel?,
    listState: LazyListState = rememberLazyListState(),
    showMainModels: Boolean,
    toggleMainModels: () -> Unit
) {

    var showDescription by remember { mutableStateOf(false) } // Toggles description visibility

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            if (showDescription && currentModel != null) {
                // 🔹 Show only the Expandable Card (description)
                item {
                    ExpandableCard(
                        title = currentModel.name,
                        description = "${currentModel.name} leverages Kotlin Filament technology to create highly detailed and interactive visualizations of human anatomy. This innovative approach enhances educational experiences and promotes a deeper understanding of the human body’s complex functions and interconnections."
                    )
                }
            } else {
                // 🔹 Show the Model List


                items(models.chunked(3)) { chunk ->
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            chunk.forEach { model ->
                                Surface(
                                    onClick = { onModelSelect(model) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    shadowElevation = 8.dp,
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.surface,
                                ) {
                                    ModelListItem(model)
                                }
                            }
                            if (chunk.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
        if (currentModel != null) {
            Button(
                onClick = { toggleMainModels() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) // Rich Red button
            ) {
                Text(
                    if (showMainModels) "View Subparts" else "Back to Models",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // 🔹 Floating Button - Toggles between Description & Model List
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val modelRowsHeight = screenHeight * 0.40f
        val buttonSize = modelRowsHeight * 0.3f
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
                    text = if (showDescription) "M" else "D", // Show "M" when in description mode, "D" when in model list
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ModelListItem(model: AnatomyModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val imagePath = "file:///android_asset/image/${
            model.name.replace(" ", "").lowercase()
        }.png"

        // 🔹 Box ensures consistent space
        Box(
            modifier = Modifier
                .size(140.dp) // Fixed size for consistency
                .clip(MaterialTheme.shapes.medium)
        ) {
            Image(
                painter = rememberAsyncImagePainter(imagePath),
                contentDescription = model.name,
                contentScale = ContentScale.Fit, // Ensures consistent size
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = model.name,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Tap to view",
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModelRowsPreview() {
    val sampleModels = listOf(
        AnatomyModel("Splanchnology", "models/Splanchnology.glb"),
        AnatomyModel("Neurology", "models/Neurology.glb"),
        AnatomyModel("Myology", "models/Myology.glb")
    )

    var showMainModels by remember { mutableStateOf(true) } // Toggle state

    AnatomyInsightTheme(darkTheme = true) {
        ModelRows(
            models = sampleModels,
            onModelSelect = {}, // No-op for preview
            currentModel = sampleModels.first(), // Show first model as selected
            showMainModels = showMainModels,
            toggleMainModels = { showMainModels = !showMainModels }
        )
    }
}

