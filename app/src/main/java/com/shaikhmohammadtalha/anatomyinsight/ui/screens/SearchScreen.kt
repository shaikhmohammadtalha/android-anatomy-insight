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

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shaikhmohammadtalha.anatomyinsight.data.SubpartEntity
import com.shaikhmohammadtalha.anatomyinsight.ui.components.BottomNavBar
import com.shaikhmohammadtalha.anatomyinsight.viewmodel.ModelViewModel

/**
 * Composable screen for searching anatomical subparts by scientific name.
 * Displays matching results and navigates to the selected model/subpart.
 *
 * @param navController Navigation controller for screen transitions
 * @param modelViewModel ViewModel used to fetch subpart data
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController, modelViewModel: ModelViewModel
) {
    var searchQuery by remember { mutableStateOf("") }

    // Reactive list of matching subparts from the database
    val searchResults by modelViewModel.searchSubpartsByScientificName(searchQuery)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController, onSearchClick = {})
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Input field for user queries
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search scientific names...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                Text("No results found", style = MaterialTheme.typography.bodyMedium)
            }

            // Display list of matched subparts
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                searchResults.forEach { subpart ->
                    SearchResultItem(subpart, navController)
                }
            }
        }
    }
}

/**
 * Card item representing a single search result.
 * Triggers navigation to the model screen and loads the subpart when clicked.
 *
 * @param subpart The subpart entity from the database
 * @param navController Used to navigate back to the main screen with selected data
 */
@Composable
fun SearchResultItem(subpart: SubpartEntity, navController: NavHostController) {
    val firstLine = subpart.description.substringBefore("\n").trim()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = MaterialTheme.colorScheme.outline,
                spotColor = MaterialTheme.colorScheme.outline
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable {
                    // Pass subpart and model IDs to main screen
                    val mainBackStackEntry = navController.getBackStackEntry("main")
                    mainBackStackEntry.savedStateHandle["selectedSubpartId"] = subpart.id
                    mainBackStackEntry.savedStateHandle["selectedModelId"] = subpart.modelId

                    navController.navigate("main") {
                        popUpTo("main") { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = subpart.scientificName,
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatText(firstLine),
                    style = MaterialTheme.typography.bodySmall
                )
            }
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
