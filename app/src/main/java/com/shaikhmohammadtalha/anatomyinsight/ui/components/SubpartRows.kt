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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shaikhmohammadtalha.anatomyinsight.data.HistoryEntity
import com.shaikhmohammadtalha.anatomyinsight.data.SubpartEntity
import com.shaikhmohammadtalha.anatomyinsight.ui.model.AnatomyModel
import com.shaikhmohammadtalha.anatomyinsight.viewmodel.ModelViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Displays the list of anatomical subparts for the selected model.
 * Tapping a subpart loads its 3D model; tapping the ℹ️ icon opens a detail bottom sheet
 * with scientific name, full description, personal notes, and "Also Viewed" chips.
 *
 * @param subparts List of subparts to display
 * @param onSubpartSelect Callback triggered when a subpart row is tapped (loads 3D model)
 * @param currentModel Currently selected main model
 * @param selectedSubpart Currently highlighted subpart
 * @param showMainModels Whether the model rail is currently shown
 * @param toggleMainModels Callback to switch back to the model rail
 * @param viewModel ViewModel for subpart details, notes, and history
 * @param listState LazyColumn scroll state
 * @param onHistoryRecord Callback to record a history entry when a subpart is selected
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    onHistoryRecord: (Int, String, String) -> Unit = { _, _, _ -> },
    fullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {}
) {
    var sheetSubpart by remember { mutableStateOf<AnatomyModel?>(null) }
    var showDescription by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val displayedSubparts = remember(subparts) {
        subparts.drop(1) // skip the first item (the model itself)
    }

    val activeSubpart = selectedSubpart ?: displayedSubparts.firstOrNull()
    val activeSubpartEntity by produceState<SubpartEntity?>(initialValue = null, activeSubpart) {
        activeSubpart?.let { subpart ->
            viewModel.getSubpartByName(subpart.name).collect { value = it }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Subpart list ───────────────────────────────────────────────
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                items(displayedSubparts, key = { it.name }) { subpart ->
                    val subpartEntity by produceState<SubpartEntity?>(
                        initialValue = null, subpart
                    ) {
                        viewModel.getSubpartByName(subpart.name).collect { value = it }
                    }

                    val isSelected = selectedSubpart?.name == subpart.name

                    ListItem(
                        headlineContent = {
                            Text(
                                text = subpart.name.replace("_", " "),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent = {
                            subpartEntity?.scientificName?.let { sci ->
                                Text(
                                    text = sci,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontStyle = FontStyle.Italic
                                    ),
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = { sheetSubpart = subpart }) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Details",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.background
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSubpartSelect(subpart)
                                subpartEntity?.let { entity ->
                                    onHistoryRecord(
                                        entity.id,
                                        entity.name,
                                        currentModel?.name ?: ""
                                    )
                                }
                            }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            // ── Collapsible description card with notes ────────────────────
            AnimatedVisibility(
                visible = showDescription && activeSubpartEntity != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                activeSubpartEntity?.let { entity ->
                    DescriptionCard(
                        title = entity.name.replace("_", " "),
                        scientificName = entity.scientificName,
                        description = entity.description,
                        subpartId = entity.id,
                        viewModel = viewModel,
                        onClose = { showDescription = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Compact control row ────────────────────────────────────────
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { toggleMainModels() }) {
                    Text(
                        text = "← Back to Models",
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
        }

        // Snackbar for note-deleted undo
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // ── Subpart Detail Bottom Sheet ────────────────────────────────────────
    sheetSubpart?.let { subpart ->
        val sheetEntity by produceState<SubpartEntity?>(initialValue = null, subpart) {
            viewModel.getSubpartByName(subpart.name).collect { value = it }
        }
        val alsoViewed by viewModel
            .getRecentHistoryForModel(currentModel?.name ?: "")
            .collectAsState(initial = emptyList())

        ModalBottomSheet(
            onDismissRequest = { sheetSubpart = null },
            sheetState = sheetState
        ) {
            sheetEntity?.let { entity ->
                SubpartDetailSheet(
                    entity = entity,
                    alsoViewed = alsoViewed,
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    onAlsoViewedClick = { historyEntry ->
                        val target = subparts.find { it.name == historyEntry.subpartName }
                        target?.let {
                            onSubpartSelect(it)
                            scope.launch { sheetState.hide() }
                            sheetSubpart = null
                        }
                    }
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading…", color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

/**
 * Content of the subpart detail bottom sheet.
 * Shows name, scientific name, full formatted description, personal notes (auto-saved),
 * and "Also Viewed" suggestion chips.
 *
 * @param entity The [SubpartEntity] being shown
 * @param alsoViewed Up to 3 recently-viewed subparts from the same system
 * @param viewModel ViewModel for note persistence
 * @param snackbarHostState For "note deleted" undo snackbar
 * @param onAlsoViewedClick Callback when an "Also Viewed" chip is tapped
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubpartDetailSheet(
    entity: SubpartEntity,
    alsoViewed: List<HistoryEntity>,
    viewModel: ModelViewModel,
    snackbarHostState: SnackbarHostState,
    onAlsoViewedClick: (HistoryEntity) -> Unit
) {
    val scope = rememberCoroutineScope()

    // Load existing note
    val savedNote by viewModel.getNoteForSubpart(entity.id)
        .collectAsState(initial = null)

    var noteText by remember(savedNote) { mutableStateOf(savedNote?.noteText ?: "") }
    var showSaved by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    // Auto-save note with 600 ms debounce
    LaunchedEffect(noteText) {
        if (noteText == (savedNote?.noteText ?: "")) return@LaunchedEffect
        delay(600)
        viewModel.upsertNote(entity.id, noteText)
        showSaved = true
        delay(1500)
        showSaved = false
    }

    if (isFullscreen) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = entity.name.replace("_", " "),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = entity.scientificName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { isFullscreen = false }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Exit Fullscreen"
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { isFullscreen = false }) {
                                    Icon(
                                        imageVector = Icons.Default.FullscreenExit,
                                        contentDescription = "Exit Fullscreen"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "DESCRIPTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatDescriptionAnnotated(entity.description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📝 MY NOTES",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AnimatedVisibility(visible = showSaved, enter = fadeIn(), exit = fadeOut()) {
                                    Text(
                                        text = "Saved ✓",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                if (noteText.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            val deletedText = noteText
                                            noteText = ""
                                            viewModel.deleteNote(entity.id)
                                            scope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Note deleted",
                                                    actionLabel = "Undo"
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    noteText = deletedText
                                                    viewModel.upsertNote(entity.id, deletedText)
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Note",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            placeholder = { Text("Write your notes, observations, or study reminders here…") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 8,
                            maxLines = 20,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entity.name.replace("_", " "),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = entity.scientificName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { isFullscreen = true }) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }

        HorizontalDivider()

        // ── Description ────────────────────────────────────────────────────
        Text(
            text = "Description",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = formatDescriptionAnnotated(entity.description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        // ── Notes ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "📝 My Notes",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(visible = showSaved, enter = fadeIn(), exit = fadeOut()) {
                    Text(
                        "Saved ✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                if (noteText.isNotBlank()) {
                    IconButton(
                        onClick = {
                            val deletedText = noteText
                            noteText = ""
                            viewModel.deleteNote(entity.id)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Note deleted",
                                    actionLabel = "Undo"
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    noteText = deletedText
                                    viewModel.upsertNote(entity.id, deletedText)
                                }
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            placeholder = { Text("Type your notes here…") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        // ── Also Viewed chips ──────────────────────────────────────────────
        val relevantHistory = alsoViewed.filter {
            it.subpartName != entity.name
        }.take(3)

        if (relevantHistory.isNotEmpty()) {
            HorizontalDivider()
            Text(
                "Also Viewed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                relevantHistory.forEach { historyEntry ->
                    SuggestionChip(
                        onClick = { onAlsoViewedClick(historyEntry) },
                        label = {
                            Text(
                                historyEntry.subpartName.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Formats a description string into an [AnnotatedString] with bold (**text**) and
 * bullet (- item) markup rendered properly.
 */
@Composable
fun formatDescriptionAnnotated(description: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = description.split("\n").filter { it.isNotBlank() }
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            val textToProcess = if (trimmed.startsWith("-")) {
                append("• ")
                trimmed.removePrefix("-").trim()
            } else {
                trimmed
            }
            // Handle **bold** markup
            val regex = Regex("\\*\\*(.*?)\\*\\*")
            val matches = regex.findAll(textToProcess)
            var lastIndex = 0
            for (match in matches) {
                append(textToProcess.substring(lastIndex, match.range.first))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[1])
                }
                lastIndex = match.range.last + 1
            }
            if (lastIndex < textToProcess.length) {
                append(textToProcess.substring(lastIndex))
            }
            if (index < lines.lastIndex) append("\n")
        }
    }
}
