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
package com.shaikhmohammadtalha.anatomyinsight.ui.utils

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

/**
 * A composable that intercepts the back press to implement a "double tap to exit" behavior.
 *
 * @param navController The navigation controller used to determine the current route.
 * @param exitThresholdMs Time window (in milliseconds) within which two back presses will exit the app.
 */
@Composable
fun DoubleBackToExitHandler(
    navController: NavHostController,
    exitThresholdMs: Long = 2000L
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var backPressedTime by remember { mutableStateOf(0L) }

    BackHandler {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        val now = System.currentTimeMillis()

        if (currentRoute == "main") {
            // If back is pressed twice within the threshold, exit the app
            if (now - backPressedTime < exitThresholdMs) {
                activity?.finish()
            } else {
                // First back press: show a warning toast
                backPressedTime = now
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        } else {
            // For other screens, simply pop the navigation stack
            navController.popBackStack()
        }
    }
}
