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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shaikhmohammadtalha.anatomyinsight.ui.components.BottomNavBar

/**
 * Composable screen displaying various app settings options including themes, graphics, and legal information.
 * Also allows copying application logs to the clipboard for debugging purposes.
 *
 * @param navController Navigation controller used to route to different settings screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current

    // List of available settings options
    val settingsOptions = listOf(
        "Themes",
        "Graphics",
        "Privacy Policy",
        "About App",
        "Copy Logs",
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                onSearchClick = { navController.navigate("search") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Dynamically build each setting item
            settingsOptions.forEach { option ->
                SettingCard(option = option) {
                    when (option) {
                        "Graphics" -> navController.navigate("graphics_settings")
                        "Themes" -> navController.navigate("theme_settings")
                        "Privacy Policy" -> navController.navigate("privacy_policy")
                        "About App" -> navController.navigate("credits")
                        "Copy Logs" -> {
                            // Copies device logs to the clipboard for reporting/debugging
                            val logs = getAppLogs()
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("App Logs", logs)
                            clipboard.setPrimaryClip(clip)

                            Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable composable that renders a card-style row for a settings option.
 *
 * @param option The text label to display
 * @param onClick Callback triggered when the card is clicked
 */
@Composable
fun SettingCard(option: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Retrieves Android logcat output from the system.
 * This function is useful for debugging or reporting issues.
 *
 * @return Full log output or error message if retrieval fails
 */
fun getAppLogs(): String {
    return try {
        val process = Runtime.getRuntime().exec("logcat -d")
        val reader = process.inputStream.bufferedReader()
        val log = reader.use { it.readText() }
        log
    } catch (e: Exception) {
        "Failed to retrieve logs: ${e.message}"
    }
}
