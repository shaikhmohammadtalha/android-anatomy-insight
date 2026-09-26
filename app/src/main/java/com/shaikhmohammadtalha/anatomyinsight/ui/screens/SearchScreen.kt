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
package com.shaikhmohammadtalha.anatomyinsight.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shaikhmohammadtalha.anatomyinsight.data.SubpartEntity
import com.shaikhmohammadtalha.anatomyinsight.ui.components.AppNavigationRail
import com.shaikhmohammadtalha.anatomyinsight.ui.components.BottomNavBar
import com.shaikhmohammadtalha.anatomyinsight.viewmodel.ModelViewModel

/** Maps model IDs 1–6 to system names. Adjust if DB order changes. */
private val modelIdToName = mapOf(
    1 to "Angiology",
    2 to "Arthrology",
    3 to "MuscularInsertions",
    4 to "Myology",
    5 to "Neurology",
    6 to "Splanchnology"
)

/**
 * Full-screen search experience for finding anatomical subparts by scientific name.
 * Auto-focuses the search field, shows a result count, idle state, and system badge per result.
 * No bottom nav bar — search is a focused, dedicated flow.
 *
 * @param navController Navigation controller for screen transitions
 * @param modelViewModel ViewModel used to fetch subpart data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    modelViewModel: ModelViewModel
) {
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600 ||
            configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val searchResults by modelViewModel.searchSubpartsByScientificName(searchQuery)
        .collectAsState(initial = emptyList())

    // Auto-focus the search field when the screen opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val onBackClick = {
        if (!navController.popBackStack()) {
            navController.navigate("main") {
                popUpTo("main") { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    val searchContent = @Composable { paddingModifier: Modifier ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(paddingModifier),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Search field — auto-focused
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search scientific names…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Result count or idle hint
                AnimatedVisibility(
                    visible = searchQuery.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "${searchResults.size} result${if (searchResults.size != 1) "s" else ""}" +
                                " for \"$searchQuery\"",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Idle state
                if (searchQuery.isBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant
                            )
                            Text(
                                "Search across 2,370 subparts",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                "Type a scientific or common name",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                } else if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant
                            )
                            Text(
                                "No results found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    // Results list
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(searchResults) { subpart ->
                            SearchResultItem(
                                subpart = subpart,
                                query = searchQuery,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }

    if (isWideScreen) {
        Row(modifier = Modifier.fillMaxSize()) {
            AppNavigationRail(navController = navController, activeTab = "search")
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Search Anatomy") },
                        navigationIcon = {
                            IconButton(onClick = { onBackClick() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        windowInsets = TopAppBarDefaults.windowInsets
                    )
                }
            ) { paddingValues ->
                searchContent(Modifier.padding(paddingValues))
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Search Anatomy") },
                    navigationIcon = {
                        IconButton(onClick = { onBackClick() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    windowInsets = TopAppBarDefaults.windowInsets
                )
            },
            bottomBar = {
                BottomNavBar(navController = navController, activeTab = "search")
            }
        ) { paddingValues ->
            searchContent(Modifier.padding(paddingValues))
        }
    }
}

/**
 * A single search result card showing scientific name (with match highlighted),
 * first line of description, and a system badge chip.
 *
 * @param subpart The subpart entity from the database
 * @param query The current search query for highlighting
 * @param navController Used to navigate back to the main screen with selected data
 */
@Composable
fun SearchResultItem(
    subpart: SubpartEntity,
    query: String,
    navController: NavHostController
) {
    val firstLine = subpart.description.substringBefore("\n").trim()
    val systemName = modelIdToName[subpart.modelId] ?: "Unknown"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val mainBackStackEntry = navController.getBackStackEntry("main")
                mainBackStackEntry.savedStateHandle["selectedSubpartId"] = subpart.id
                mainBackStackEntry.savedStateHandle["selectedModelId"] = subpart.modelId
                mainBackStackEntry.savedStateHandle["targetTab"] = "main"
                navController.navigate("main") {
                    popUpTo("main") { inclusive = false }
                    launchSingleTop = true
                    restoreState = true
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Scientific name with query highlighted
                Text(
                    text = highlightQuery(subpart.scientificName, query),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                // System badge
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            systemName,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Common name (italic)
            Text(
                text = subpart.name.replace("_", " "),
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Description first line preview
            Text(
                text = formatText(firstLine),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

/**
 * Returns an [AnnotatedString] with any occurrences of [query] highlighted
 * using the primaryContainer background color.
 */
@Composable
fun highlightQuery(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var start = 0
        while (true) {
            val idx = lowerText.indexOf(lowerQuery, start)
            if (idx == -1) {
                append(text.substring(start))
                break
            }
            append(text.substring(start, idx))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(text.substring(idx, idx + query.length))
            }
            start = idx + query.length
        }
    }
}

/**
 * Formats a string to render bold text using **double asterisks** markup.
 *
 * @param input Raw text input potentially containing bold syntax
 * @return Annotated string with styles applied
 */
@Composable
fun formatText(input: String): AnnotatedString {
    return buildAnnotatedString {
        val regex = Regex("\\*\\*(.*?)\\*\\*")
        val matches = regex.findAll(input)
        var lastIndex = 0
        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            append(input.substring(lastIndex, start))
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1])
            }
            lastIndex = end
        }
        if (lastIndex < input.length) {
            append(input.substring(lastIndex))
        }
    }
}
