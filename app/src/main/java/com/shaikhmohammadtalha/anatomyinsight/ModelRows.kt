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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

    Box(modifier = Modifier.fillMaxSize()) {
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

        // Toggle button to switch between the model list and the description view
        if (currentModel != null) {
            Button(
                onClick = { toggleMainModels() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (showMainModels) "View Subparts" else "Back to Models",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Floating button to toggle between the description view and the list view
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val modelRowsHeight = screenHeight * 0.40f
        val buttonSize = modelRowsHeight * 0.3f
        if (currentModel != null) {
            FloatingActionButton(
                onClick = { showDescription = !showDescription },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(buttonSize)
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = if (showDescription) "M" else "D",
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
        // Construct the image path from the model name
        val imagePath = "file:///android_asset/image/${model.name.replace(" ", "").lowercase()}.png"

        // Display the model image with consistent size and rounded corners
        Box(
            modifier = Modifier
                .size(140.dp)
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
