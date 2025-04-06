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
import com.shaikhmohammadtalha.data.AnatomyModelEntity
import com.shaikhmohammadtalha.viewmodel.ModelViewModel

@Composable
fun ModelRows(
    models: List<AnatomyModel>, // List of models computed in the parent composable
    viewModel: ModelViewModel,  // ViewModel providing data
    onModelSelect: (AnatomyModel) -> Unit, // Callback when a model is selected
    currentModel: AnatomyModel?, // Currently selected model (if any)
    listState: LazyListState, // Preserved scroll state for the model list
    showMainModels: Boolean, // Flag to indicate if the main model list is displayed
    toggleMainModels: () -> Unit // Callback to toggle between models and subparts
) {
    // Local state to control whether the model description (ExpandableCard) is shown
    var showDescription by remember { mutableStateOf(false) }

    // Separate scroll state for the description view to avoid interfering with list state
    val descriptionScrollState = rememberScrollState()

    // Obtain model details for the currently selected model asynchronously
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
                // Use a scrollable Column for the description view to maintain a separate scroll state
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
                // Display the main model list using the provided LazyListState
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
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
                                        onClick = { onModelSelect(model) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 8.dp)
                                            .border(
                                                width = if (currentModel?.name == model.name) 1.dp else 0.dp, // Add border if selected
                                                color = if (currentModel?.name == model.name) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                shape = MaterialTheme.shapes.large
                                            )
                                            .shadow(
                                                elevation = if (currentModel?.name == model.name) 12.dp else 8.dp, // Increase elevation for selected
                                                shape = MaterialTheme.shapes.large
                                            ),
                                        shadowElevation = 8.dp,
                                        shape = MaterialTheme.shapes.large,
                                        color = MaterialTheme.colorScheme.surface,
                                    ) {
                                        ModelListItem(
                                            model, currentModel,
                                            viewModel = viewModel
                                        )
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
        }
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
                            .weight(0.8f)
                            .shadow(
                                12.dp, shape = RoundedCornerShape(12.dp), // ✅ Glow Effect
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            .border(
                                3.dp, Brush.radialGradient( // ✅ Gradient Border
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
                        onClick = { showDescription = !showDescription },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .weight(0.2f)
                            .shadow(
                                12.dp, shape = RoundedCornerShape(12.dp), // ✅ Glow Effect
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            .border(
                                3.dp, Brush.radialGradient( // ✅ Gradient Border
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                ), RoundedCornerShape(20.dp)
                            )
                    ) {
                        Text(
                            text = if (showDescription) "M" else "D",
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

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
        // Construct the image path from the model name
        val imagePath = "file:///android_asset/image/${model.name.replace(" ", "").lowercase()}.png"

        // Display the model image with consistent size and rounded corners
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

        // Display the model name and a prompt to tap
        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            modelDetails?.scientificName?.let { scientificName ->
                val cleanedName =
                    scientificName.substringBefore("(").trim() // ✅ Remove content after "("

                Text(
                    text = cleanedName, // ✅ Display only the cleaned name
                    color = if (currentModel?.name == model.name) MaterialTheme.colorScheme.primary // ✅ RichRed for selected model
                    else MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }

            if (currentModel?.name != model.name) {
                Text(
                    text = "Tap to view",
                    Modifier
                        .padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
