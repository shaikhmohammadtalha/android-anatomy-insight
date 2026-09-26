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

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Credits screen with icon-card sections for each attribution category.
 * Links to GitHub, email, and licence URLs are tappable via Android's Intent system.
 *
 * @param navController Used for back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(navController: NavController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credits & Attributions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            Text(
                text = "Anatomy Insight is built on a foundation of open-source tools, " +
                        "3D models, and educational literature. Thank you to everyone whose " +
                        "work made this project possible.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            // ── 3D Models ──────────────────────────────────────────────────
            AttributionCard(
                icon = { Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(24.dp)) },
                title = "3D Models",
                body = "Z-Anatomy (CC BY-SA 4.0)\n" +
                        "Original models adapted from BodyParts3D.\n" +
                        "Modifications include renaming, restructuring into 2,370 subparts, " +
                        "and mesh optimisation (scaling, repair, simplification).",
                linkLabel = "z-anatomy.com",
                linkUrl = "https://www.z-anatomy.com/"
            ) { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }

            // ── HDR Environment ────────────────────────────────────────────
            AttributionCard(
                icon = { Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(24.dp)) },
                title = "HDR Environment",
                body = "lightroom_14b.hdr from Google Model Viewer (Apache License 2.0).",
                linkLabel = "google/model-viewer",
                linkUrl = "https://github.com/google/model-viewer"
            ) { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }

            // ── Educational Descriptions ───────────────────────────────────
            AttributionCard(
                icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(24.dp)) },
                title = "Educational Descriptions",
                body = "• OpenStax — Anatomy & Physiology (CC BY 4.0)\n" +
                        "• Gray's Anatomy 1918 Edition (Public Domain)\n" +
                        "• MedlinePlus — U.S. National Library of Medicine (Public Domain)",
                linkLabel = "openstax.org",
                linkUrl = "https://openstax.org/details/books/anatomy-and-physiology"
            ) { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }

            // ── Licence ────────────────────────────────────────────────────
            AttributionCard(
                icon = { Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(24.dp)) },
                title = "Licence",
                body = "This application is licensed under the Apache License 2.0.\n" +
                        "3D content follows CC BY-SA 4.0 where applicable.\n" +
                        "Educational descriptions follow their respective open licences.",
                linkLabel = "Apache 2.0",
                linkUrl = "https://www.apache.org/licenses/LICENSE-2.0"
            ) { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }

            // ── Author ─────────────────────────────────────────────────────
            AttributionCard(
                icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(24.dp)) },
                title = "Developer",
                body = "Developed by Shaikh Mohammad Talha.\n" +
                        "Feedback, stars, and contributions are always welcome!",
                linkLabel = "@shaikhmohammadtalha",
                linkUrl = "https://github.com/shaikhmohammadtalha"
            ) { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
}

/**
 * Reusable attribution card with icon, title, body text and an optional tappable link.
 *
 * @param icon Slot for a leading icon composable
 * @param title Section title
 * @param body Attribution body text
 * @param linkLabel Visible label for the link
 * @param linkUrl URL to open when the link is tapped
 * @param onLinkClick Callback with the URL to open
 */
@Composable
fun AttributionCard(
    icon: @Composable () -> Unit,
    title: String,
    body: String,
    linkLabel: String,
    linkUrl: String,
    onLinkClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                icon()
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = { onLinkClick(linkUrl) },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "↗ $linkLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
