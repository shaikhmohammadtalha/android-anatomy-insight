package com.shaikhmohammadtalha.anatomyinsight.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
 * Composable screen presenting the app’s privacy policy and license attributions.
 * Ensures transparency by outlining how user data is handled and where content originates.
 *
 * @param navController Used for back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
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
                .padding(16.dp)
        ) {
            // Section: General privacy policy
            Text("Privacy Policy", style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Anatomy Insight does not collect, store, or share any personal data.\n\n" +
                        "All processing and rendering of 3D anatomical models occurs locally on your device. " +
                        "The app operates entirely offline and does not connect to any servers or third-party services.\n\n" +
                        "However, the Android operating system or your device manufacturer may collect diagnostic or usage data as governed by your system settings.\n\n" +
                        "We respect user privacy and are committed to maintaining a transparent and safe experience.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(24.dp))

            // Section: Licensing and attribution
            Text("Licenses & Attribution", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(8.dp))

            Text(
                text = "• 3D models sourced from Z-Anatomy under CC BY-SA 4.0\n" +
                        "• HDR lighting file (lightroom_14b.hdr) licensed under Apache 2.0\n" +
                        "• Educational descriptions from OpenStax, Gray’s Anatomy (1918), and MedlinePlus (public domain)",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
