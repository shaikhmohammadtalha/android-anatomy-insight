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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.shaikhmohammadtalha.anatomyinsight.data.AnatomyModelEntity
import com.shaikhmohammadtalha.anatomyinsight.data.SubpartEntity
import com.shaikhmohammadtalha.anatomyinsight.ui.model.AnatomyModel
import com.shaikhmohammadtalha.anatomyinsight.viewmodel.ModelViewModel

/**
 * Displays anatomical models in a horizontal scrolling rail and, when a model is selected,
 * shows a collapsible description card. Compact TextButton controls replace the old large Button row.
 *
 * @param models List of all available anatomical models
 * @param viewModel ViewModel providing model metadata
 * @param onModelSelect Callback triggered when a model card is tapped
 * @param currentModel Currently selected model
 * @param listState Scroll state of the LazyRow for retaining position
 * @param showMainModels Whether the model rail or the subpart list is currently shown
 * @param toggleMainModels Callback to switch between model and subpart views
 * @param fullscreen Whether the panel is currently in fullscreen mode
 * @param onToggleFullscreen Callback to toggle fullscreen for this panel
 */
@Composable
fun ModelRows(
    models: List<AnatomyModel>,
    viewModel: ModelViewModel,
    onModelSelect: (AnatomyModel) -> Unit,
    currentModel: AnatomyModel?,
    listState: LazyListState,
    showMainModels: Boolean,
    toggleMainModels: () -> Unit,
    fullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {}
) {
    var showDescription by remember { mutableStateOf(false) }

    // Collect model metadata for description card
    val modelDetails by produceState<AnatomyModelEntity?>(initialValue = null, currentModel) {
        currentModel?.let { model ->
            viewModel.getModelByName(model.name).collect { value = it }
        }
    }

    val modelSubpart by produceState<SubpartEntity?>(initialValue = null, currentModel) {
        currentModel?.let { model ->
            viewModel.getSubpartByName(model.name).collect { value = it }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Horizontal model rail ──────────────────────────────────────────
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(models) { model ->
                ModelCard(
                    model = model,
                    isSelected = currentModel?.name == model.name,
                    viewModel = viewModel,
                    onClick = { onModelSelect(model) }
                )
            }
        }

        // ── Compact control row (always visible right below the model rail) ─
        if (currentModel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { toggleMainModels() }) {
                    Text(
                        text = if (showMainModels) "View Subparts →" else "← Back to Models",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showDescription = !showDescription }) {
                        Text(
                            text = if (showDescription) "Hide Description" else "Description",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    IconButton(onClick = onToggleFullscreen, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (fullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (fullscreen) "Exit fullscreen" else "Expand panel",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
        }

        // ── Collapsible description card with notes ────────────────────────
        AnimatedVisibility(
            visible = showDescription && currentModel != null && modelDetails != null,
            enter = expandVertically(),
            exit = shrinkVertically(),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
        ) {
            modelDetails?.let { details ->
                DescriptionCard(
                    title = currentModel?.name?.replace("_", " ") ?: "",
                    scientificName = details.scientificName,
                    description = details.description,
                    subpartId = modelSubpart?.id,
                    viewModel = viewModel,
                    onClose = { showDescription = false },
                    onViewSubparts = { toggleMainModels() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * A single model card in the horizontal rail.
 * Selected cards get a primary border and full opacity; unselected cards are slightly dimmed.
 *
 * @param model The model to display
 * @param isSelected Whether this card is currently selected
 * @param viewModel Used to fetch scientific name metadata
 * @param onClick Callback when this card is tapped
 */
@Composable
fun ModelCard(
    model: AnatomyModel,
    isSelected: Boolean,
    viewModel: ModelViewModel,
    onClick: () -> Unit
) {
    val modelDetails by produceState<AnatomyModelEntity?>(initialValue = null, model) {
        viewModel.getModelByName(model.name).collect { value = it }
    }

    val imagePath = "file:///android_asset/image/${model.name.replace(" ", "").lowercase()}.png"

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(120.dp)
            .graphicsLayer { alpha = if (isSelected) 1f else 0.7f },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        shadowElevation = if (isSelected) 8.dp else 2.dp,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Model thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imagePath),
                    contentDescription = model.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Scientific name (truncated to 2 lines)
            val displayName = modelDetails?.scientificName
                ?.substringBefore("(")
                ?.trim()
                ?: model.name.replace("_", " ")

            Text(
                text = displayName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
