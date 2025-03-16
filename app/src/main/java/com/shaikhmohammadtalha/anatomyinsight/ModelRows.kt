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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.shaikhmohammadtalha.anatomyinsight.ui.theme.AnatomyInsightTheme
import com.shaikhmohammadtalha.anatomyinsight.ui.theme.ExpandableCardText
import com.shaikhmohammadtalha.anatomyinsight.ui.theme.ModelCardBackground

@Composable
fun ModelRows(
    models: List<AnatomyModel>,
    onModelSelect: (AnatomyModel) -> Unit, // Callback for model selection
    currentModel: AnatomyModel?,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        state = listState, // Apply scroll state here
        modifier = Modifier.fillMaxSize(),
    ) {
        // Show expandable card if a model is selected
        currentModel?.let {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    ExpandableCard(
                        title = it.name,
                        description = "${it.name} leverages Kotlin Filament technology to create highly detailed and interactive visualizations of human anatomy. This innovative approach enhances educational experiences and promotes a deeper understanding of the human body’s complex functions and interconnections."
                    )
                }
            }
        }

        // Show the list of models
        // Show the list of models
        items(models.chunked(2)) { pair ->
            Surface( // Wrap the entire row in a Surface to apply background color
                color = Color(0xFF757575), // Dark Gray (adjust as needed)
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp), // Add some spacing
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    pair.forEach { model ->
                        Surface(
                            onClick = { onModelSelect(model) }, // Load the selected model

                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            shape = MaterialTheme.shapes.large,
                            color = ModelCardBackground // Keep inner card theme
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val imagePath = "file:///android_asset/image/${
                                    model.name.replace(" ", "").lowercase()
                                }.png"

                                Image(
                                    painter = rememberAsyncImagePainter(imagePath),
                                    contentDescription = model.name,
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                )

                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = model.name,
                                        color = ExpandableCardText, // Keep inner card theme
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Tap to view",
                                        color = ExpandableCardText, // Keep inner card theme
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    if (pair.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
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

    AnatomyInsightTheme {
        ModelRows(
            models = sampleModels,
            onModelSelect = {}, // No-op for preview
            currentModel = sampleModels.first() // Show first model as selected
            // Simulated screen height
        )
    }
}
