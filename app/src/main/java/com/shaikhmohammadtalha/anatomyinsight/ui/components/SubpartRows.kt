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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shaikhmohammadtalha.anatomyinsight.ui.model.AnatomyModel
import com.shaikhmohammadtalha.anatomyinsight.data.SubpartEntity
import com.shaikhmohammadtalha.anatomyinsight.viewmodel.ModelViewModel

/**
 * Displays a list of subparts for the selected anatomical model.
 * Allows switching between the subpart list and a description view.
 *
 * @param subparts List of subparts to display
 * @param onSubpartSelect Callback triggered when a subpart is selected
 * @param currentModel Currently selected main model
 * @param selectedSubpart Currently selected subpart
 * @param showMainModels Flag indicating if the main model list is currently shown
 * @param toggleMainModels Callback to switch between model and subpart views
 * @param viewModel ViewModel for retrieving subpart details
 * @param listState Scroll state to retain position across recompositions
 */
@Composable
fun SubpartRows(
    subparts: List<AnatomyModel>,
    onSubpartSelect: (AnatomyModel) -> Unit,
    currentModel: AnatomyModel?,
    selectedSubpart: AnatomyModel?,
    showMainModels: Boolean,
    toggleMainModels: () -> Unit,
    viewModel: ModelViewModel,
    listState: LazyListState,
) {
    var showDescription by remember { mutableStateOf(false) }

    // Observe the selected subpart's entity details from the database
    val subpartDetails by produceState<SubpartEntity?>(initialValue = null, selectedSubpart) {
        selectedSubpart?.let { subpart ->
            viewModel.getSubpartByName(subpart.name).collect { value = it }
        }
    }

    val descriptionScrollState = rememberScrollState()

    // Optionally fetch the model's SubpartEntity (some models may also have description records)
    val modelDetails by produceState<SubpartEntity?>(initialValue = null, currentModel) {
        currentModel?.let { model ->
            viewModel.getSubpartByName(model.name).collect { value = it }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Top section: either description view or list of subparts
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.8f)
        ) {
            if (showDescription) {
                // Expandable subpart description with scroll
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(descriptionScrollState)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (selectedSubpart != null && subpartDetails != null) {
                        ExpandableCard(
                            title = subpartDetails!!.scientificName,
                            description = subpartDetails!!.description
                        )
                    } else if (modelDetails != null) {
                        ExpandableCard(
                            title = modelDetails!!.scientificName,
                            description = modelDetails!!.description
                        )
                    } else {
                        Text(
                            text = "Loading details...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                // Scrollable list of subparts with highlighting for selection
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    items(subparts.drop(1)) { subpart ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                                .clickable {
                                    println("Subpart selected: ${subpart.name}")
                                    onSubpartSelect(subpart)
                                }
                                .border(
                                    width = if (selectedSubpart?.name == subpart.name) 1.dp else 0.dp,
                                    color = if (selectedSubpart?.name == subpart.name)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .shadow(
                                    elevation = if (selectedSubpart?.name == subpart.name) 12.dp else 4.dp,
                                    shape = MaterialTheme.shapes.medium
                                ),
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
                                // Subpart icon trigger
                                IconButton(onClick = { onSubpartSelect(subpart) }) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Subpart Icon",
                                        tint = if (selectedSubpart?.name == subpart.name)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline,
                                    )
                                }

                                // Fetch and show subpart name or fallback to raw name
                                val subpartEntity by produceState<SubpartEntity?>(
                                    initialValue = null, subpart
                                ) {
                                    viewModel.getSubpartByName(subpart.name).collect { value = it }
                                }

                                Text(
                                    text = subpartEntity?.scientificName ?: subpart.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(50.dp))
                            }
                        }
                    }
                }
            }
        }

        // Bottom controls: view switch and description toggle
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
                            println("Toggled subpart description: $showDescription")
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
