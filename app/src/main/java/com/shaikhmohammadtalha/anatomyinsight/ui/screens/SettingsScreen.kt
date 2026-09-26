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

import android.app.Activity
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shaikhmohammadtalha.anatomyinsight.data.DatabaseBackupManager
import com.shaikhmohammadtalha.anatomyinsight.ui.components.AppNavigationRail
import com.shaikhmohammadtalha.anatomyinsight.ui.components.BottomNavBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Settings screen with grouped, icon-labelled navigation items.
 * Sections: Appearance, Rendering, General, with a version footer.
 *
 * @param navController Navigation controller used to route to sub-screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600 ||
            configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            scope.launch {
                val result = DatabaseBackupManager.backupDatabase(context, uri)
                isProcessing = false
                if (result.isSuccess) {
                    Toast.makeText(context, "Database backup downloaded successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Backup failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            scope.launch {
                val result = DatabaseBackupManager.restoreDatabase(context, uri)
                isProcessing = false
                if (result.isSuccess) {
                    Toast.makeText(
                        context,
                        "Database restored successfully! Restarting app...",
                        Toast.LENGTH_SHORT
                    ).show()
                    kotlinx.coroutines.delay(400)
                    DatabaseBackupManager.restartApp(context)
                } else {
                    Toast.makeText(context, "Restore failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text("Restore Database") },
            text = {
                Text(
                    "Replacing the database will overwrite your current notes, history, and models with the selected backup file. The app will restart automatically to apply the new database.\n\nDo you want to proceed and select a backup file?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmDialog = false
                        restoreLauncher.launch(arrayOf("*/*"))
                    }
                ) {
                    Text("Select Backup File", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val onBackClick = {
        if (!navController.popBackStack()) {
            navController.navigate("main") {
                popUpTo("main") { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    val content = @Composable { paddingModifier: Modifier ->
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
                    .verticalScroll(rememberScrollState())
            ) {

                // ── Appearance ──────────────────────────────────────────────────
                SettingsSectionHeader("Appearance")

                ListItem(
                    headlineContent = { Text("Theme") },
                    supportingContent = { Text("Auto, Light, Dark, Dynamic") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("theme_settings") }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // ── Rendering ───────────────────────────────────────────────────
                SettingsSectionHeader("Rendering")

                ListItem(
                    headlineContent = { Text("Graphics Quality") },
                    supportingContent = { Text("Quality, MSAA, Ambient Occlusion, Bloom") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("graphics_settings") }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // ── Data & Backup ───────────────────────────────────────────────
                SettingsSectionHeader("Data & Backup")

                ListItem(
                    headlineContent = { Text("Backup Database") },
                    supportingContent = { Text("Download database, notes, and history as a .db file") },
                    leadingContent = {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isProcessing) {
                            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
                            val defaultName = "anatomy_backup_${dateFormat.format(Date())}.db"
                            backupLauncher.launch(defaultName)
                        }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ListItem(
                    headlineContent = { Text("Restore Database") },
                    supportingContent = { Text("Replace current database with an existing .db file") },
                    leadingContent = {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isProcessing) {
                            showRestoreConfirmDialog = true
                        }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // ── General ─────────────────────────────────────────────────────
                SettingsSectionHeader("General")

                ListItem(
                    headlineContent = { Text("Privacy Policy") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("privacy_policy") }
                )

                ListItem(
                    headlineContent = { Text("Credits & Attributions") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("credits") }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                Spacer(modifier = Modifier.height(24.dp))

                // ── Footer ──────────────────────────────────────────────────────
                Text(
                    text = "v2.0 · Apache License 2.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

    if (isWideScreen) {
        Row(modifier = Modifier.fillMaxSize()) {
            AppNavigationRail(navController = navController, activeTab = "settings")
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Settings") },
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
            ) { padding ->
                content(Modifier.padding(padding))
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
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
                BottomNavBar(navController = navController, activeTab = "settings")
            }
        ) { padding ->
            content(Modifier.padding(padding))
        }
    }
}

/**
 * Section header label used to group related settings items.
 *
 * @param title The section title text
 */
@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
    )
}
