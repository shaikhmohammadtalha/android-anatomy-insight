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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Composable screen displaying credits, open-source attributions, and licensing details.
 * This screen is accessible via the settings or about section of the app.
 *
 * @param navController Used to handle back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credits & Attributions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: General credit intro
            Text("Credits", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Anatomy Insight is a Jetpack Compose-powered app for exploring 3D anatomy with Filament rendering. The following open-source resources made this project possible:",
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                thickness = 1.dp
            )

            // Section: 3D models attribution
            Text("3D Models", style = MaterialTheme.typography.titleMedium)
            Text(
                "• Z-Anatomy (CC BY-SA 4.0)\n" +
                        "• Original models adapted from BodyParts3D\n" +
                        "• Modifications include renaming, restructuring into ~2300 subparts, and optimization (e.g., scaling and mesh simplification)",
                style = MaterialTheme.typography.bodySmall
            )

            // Section: Lighting/environment assets
            Text("HDR Environment", style = MaterialTheme.typography.titleMedium)
            Text(
                "• lightroom_14b.hdr from Google Model Viewer (Apache 2.0)",
                style = MaterialTheme.typography.bodySmall
            )

            // Section: Description content sources
            Text("Educational Descriptions", style = MaterialTheme.typography.titleMedium)
            Text(
                "• OpenStax - Anatomy & Physiology (CC BY 4.0)\n" +
                        "• Gray’s Anatomy (1918 Edition) – Public Domain\n" +
                        "• MedlinePlus (U.S. National Library of Medicine) – Public Domain",
                style = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                thickness = 1.dp
            )

            // Section: Licensing information
            Text("Licenses", style = MaterialTheme.typography.titleMedium)
            Text(
                "This app follows the Apache License 2.0.\n" +
                        "Educational and 3D content follow their respective open licenses.",
                style = MaterialTheme.typography.bodySmall
            )

            // Section: Author info
            Text("Author & Contributions", style = MaterialTheme.typography.titleMedium)
            Text(
                "Developed by Shaikh Mohammad Talha\n" +
                        "GitHub: @shaikhmohammadtalha\n" +
                        "Email: shaikhmot@gmail.com\n" +
                        "Feedback, stars, and contributions are always welcome!",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

