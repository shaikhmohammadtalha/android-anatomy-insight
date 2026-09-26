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

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Bottom navigation bar with four tabs: Home, Search, History, Settings.
 *
 * @param navController Navigation controller for routing between tabs
 * @param activeTab Explicitly active tab override
 */
@Composable
fun BottomNavBar(
    navController: NavHostController,
    activeTab: String? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = activeTab ?: navBackStackEntry?.destination?.route

    NavigationBar {
        // ── Home ────────────────────────────────────────────────────────────
        NavigationBarItem(
            selected = currentDestination == "main",
            onClick = {
                navigateToTab(navController, "main", currentDestination)
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        // ── Search ──────────────────────────────────────────────────────────
        NavigationBarItem(
            selected = currentDestination == "search",
            onClick = {
                navigateToTab(navController, "search", currentDestination)
            },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        // ── History ─────────────────────────────────────────────────────────
        NavigationBarItem(
            selected = currentDestination == "history",
            onClick = {
                navigateToTab(navController, "history", currentDestination)
            },
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text("History") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        // ── Settings ────────────────────────────────────────────────────────
        NavigationBarItem(
            selected = currentDestination == "settings",
            onClick = {
                navigateToTab(navController, "settings", currentDestination)
            },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

/**
 * Tablet / Wide-screen Navigation Rail anchored to the left of the screen.
 * Conforms to Material 3 large-screen guidelines for tablets, foldables, and landscape mode.
 *
 * @param navController Navigation controller for routing between tabs
 * @param activeTab Explicitly active tab override
 */
@Composable
fun AppNavigationRail(
    navController: NavHostController,
    activeTab: String? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = activeTab ?: navBackStackEntry?.destination?.route

    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        header = {
            Icon(
                Icons.Default.ViewInAr,
                contentDescription = null,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        NavigationRailItem(
            selected = currentDestination == "main",
            onClick = { navigateToTab(navController, "main", currentDestination) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationRailItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationRailItem(
            selected = currentDestination == "search",
            onClick = { navigateToTab(navController, "search", currentDestination) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") },
            colors = NavigationRailItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationRailItem(
            selected = currentDestination == "history",
            onClick = { navigateToTab(navController, "history", currentDestination) },
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text("History") },
            colors = NavigationRailItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationRailItem(
            selected = currentDestination == "settings",
            onClick = { navigateToTab(navController, "settings", currentDestination) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            colors = NavigationRailItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

private fun navigateToTab(
    navController: NavHostController,
    targetRoute: String,
    currentRoute: String?
) {
    if (currentRoute == targetRoute) return
    navController.navigate(targetRoute) {
        popUpTo("main") {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
