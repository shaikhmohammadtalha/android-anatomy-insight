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
package com.shaikhmohammadtalha.anatomyinsight.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.shaikhmohammadtalha.anatomyinsight.ui.model.AnatomyModel
import com.shaikhmohammadtalha.anatomyinsight.data.AnatomyModelEntity
import com.shaikhmohammadtalha.anatomyinsight.viewmodel.ModelViewModel

/**
 * Displays the list of anatomical models and toggles between model list and description.
 *
 * @param models List of all available anatomical models
 * @param viewModel ViewModel providing model metadata
 * @param onModelSelect Callback triggered when a model is selected
 * @param currentModel Currently selected model
 * @param listState Scroll state of the LazyColumn for restoring position
 * @param showMainModels Boolean controlling whether models or subparts are shown
 * @param toggleMainModels Callback to toggle view between models and subparts
 */
@Composable
fun ModelRows(
    models: List<AnatomyModel>,
    viewModel: ModelViewModel,
    onModelSelect: (AnatomyModel) -> Unit,
    currentModel: AnatomyModel?,
    listState: LazyListState,
    showMainModels: Boolean,
    toggleMainModels: () -> Unit
) {
    var showDescription by remember { mutableStateOf(false) }
    val descriptionScrollState = rememberScrollState()

    // Collect model metadata if a model is selected
    val modelDetails by produceState<AnatomyModelEntity?>(initialValue = null, currentModel) {
        currentModel?.let { model ->
            viewModel.getModelByName(model.name).collect { value = it }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.8f)
        ) {
            if (showDescription && currentModel != null) {
                // Show description view with scroll support
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(descriptionScrollState)
                ) {
                    if (modelDetails != null) {
                        ExpandableCard(
                            title = modelDetails!!.scientificName,
                            description = modelDetails!!.description
                        )
                    } else {
                        Text(
                            text = "Loading model details...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                // Show model grid using chunks of 3
                LazyColumn(
                    state = listState, modifier = Modifier.fillMaxSize()
                ) {
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
                                        onClick = {
                                            println("Model selected: ${model.name}")
                                            onModelSelect(model)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 8.dp)
                                            .border(
                                                width = if (currentModel?.name == model.name) 1.dp else 0.dp,
                                                color = if (currentModel?.name == model.name)
                                                    MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outline,
                                                shape = MaterialTheme.shapes.large
                                            )
                                            .shadow(
                                                elevation = if (currentModel?.name == model.name) 12.dp else 8.dp,
                                                shape = MaterialTheme.shapes.large
                                            ),
                                        shadowElevation = 8.dp,
                                        shape = MaterialTheme.shapes.large,
                                        color = MaterialTheme.colorScheme.surface,
                                    ) {
                                        ModelListItem(model, currentModel, viewModel)
                                    }
                                }

                                // Spacer if row has fewer than 3 items
                                if (chunk.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom controls: toggle model/subpart view and toggle description
        if (currentModel != null) {
            Box(
                modifier = Modifier
                    .weight(0.15f)
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { toggleMainModels() },
                        modifier = Modifier
                            .weight(0.6f)
                            .border(
                                3.dp, Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                ), RoundedCornerShape(20.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = if (showMainModels) "View Subparts" else "Back to Models",
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            showDescription = !showDescription
                            println("Toggled model description: $showDescription")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .weight(0.4f)
                            .border(
                                3.dp, Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                ), RoundedCornerShape(20.dp)
                            )
                    ) {
                        Text(
                            text = if (showDescription) "Models" else "Description",
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays an individual model item with image and label.
 *
 * @param model The model to display
 * @param currentModel Currently selected model for styling purposes
 * @param viewModel Used to fetch metadata for the displayed model
 */
@Composable
fun ModelListItem(model: AnatomyModel, currentModel: AnatomyModel?, viewModel: ModelViewModel) {
    val modelDetails by produceState<AnatomyModelEntity?>(initialValue = null, model) {
        viewModel.getModelByName(model.name).collect { value = it }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display model image from assets
        val imagePath = "file:///android_asset/image/${model.name.replace(" ", "").lowercase()}.png"

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(MaterialTheme.shapes.medium)
        ) {
            Image(
                painter = rememberAsyncImagePainter(imagePath),
                contentDescription = model.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display scientific name or fallback name
        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            modelDetails?.scientificName?.let { scientificName ->
                val cleanedName = scientificName.substringBefore("(").trim()

                Text(
                    text = cleanedName,
                    color = if (currentModel?.name == model.name)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }

            if (currentModel?.name != model.name) {
                Text(
                    text = "Tap to view",
                    Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
