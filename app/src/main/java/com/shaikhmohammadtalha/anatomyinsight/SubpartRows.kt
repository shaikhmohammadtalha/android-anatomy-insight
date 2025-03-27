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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shaikhmohammadtalha.data.SubpartEntity
import com.shaikhmohammadtalha.viewmodel.ModelViewModel

@Composable
fun SubpartRows(
    subparts: List<AnatomyModel>, // List of subparts associated with the selected model
    onSubpartSelect: (AnatomyModel) -> Unit, // Callback when a subpart is selected
    currentModel: AnatomyModel?, // Currently selected main model
    selectedSubpart: AnatomyModel?, // Currently selected subpart (if any)
    showMainModels: Boolean, // Flag to determine whether to display models or subparts
    toggleMainModels: () -> Unit, // Callback to toggle between models and subparts
    viewModel: ModelViewModel, // ViewModel providing subpart data
    listState: LazyListState // Preserved scroll state for the subpart list
) {
    // Local state to control whether the subpart description is shown
    var showDescription by remember { mutableStateOf(false) }

    // Fetch details for the selected subpart asynchronously
    val subpartDetails by produceState<SubpartEntity?>(initialValue = null, selectedSubpart) {
        selectedSubpart?.let { subpart ->
            viewModel.getSubpartByName(subpart.name).collect { value = it }
        }
    }

    // Separate scroll state for the description view
    val descriptionScrollState = rememberScrollState()

    // Fetch details for the currently selected main model asynchronously
    val modelDetails by produceState<SubpartEntity?>(initialValue = null, currentModel) {
        currentModel?.let { model ->
            viewModel.getSubpartByName(model.name).collect { value = it }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showDescription) {
            // Scrollable column for the description section to prevent scroll conflicts
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(descriptionScrollState)
                    .background(color = MaterialTheme.colorScheme.background)
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
            // Display the subpart list with the preserved scroll state
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                items(subparts) { subpart ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                            .clickable { onSubpartSelect(subpart) },
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
                            // Button for selecting a subpart
                            IconButton(onClick = { onSubpartSelect(subpart) }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Subpart Icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Fetch subpart entity details asynchronously
                            val subpartEntity by produceState<SubpartEntity?>(initialValue = null, subpart) {
                                viewModel.getSubpartByName(subpart.name).collect { value = it }
                            }

                            // Display the scientific name if available, otherwise fallback to the subpart name
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

        // Toggle button to switch between the main model list and subpart view
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

        // Floating button to toggle between subpart description and list
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val subpartRowsHeight = screenHeight * 0.40f
        val buttonSize = subpartRowsHeight * 0.3f
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
