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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue

/**
 * Bottom navigation bar used throughout the app.
 * Provides quick access to the Home, Search, and Settings screens.
 *
 * @param navController Controller for navigating between composable destinations
 * @param onSearchClick Lambda invoked when the Search item is selected
 */
@Composable
fun BottomNavBar(
    navController: NavController,
    onSearchClick: () -> Unit
) {
    // Get the current destination from the navigation stack
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .height(64.dp),
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp
    ) {
        // Home navigation item
        NavigationBarItem(
            selected = currentDestination == "main",
            onClick = {
                if (currentDestination != "main") {
                    navController.navigate("main") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                        }
                        launchSingleTop = true
                        restoreState = false // Consider true only if state restoration is enabled
                    }
                }
            },
            icon = {
                Icon(Icons.Default.Home, contentDescription = "Home")
            },
            label = { Text("Home") }
        )

        // Search navigation item
        NavigationBarItem(
            selected = currentDestination == "search",
            onClick = {
                if (currentDestination != "search") {
                    onSearchClick()
                }
            },
            icon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            label = { Text("Search") }
        )

        // Settings navigation item
        NavigationBarItem(
            selected = currentDestination == "settings",
            onClick = {
                if (currentDestination != "settings") {
                    navController.navigate("settings")
                }
            },
            icon = {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            },
            label = { Text("Settings") }
        )
    }
}
