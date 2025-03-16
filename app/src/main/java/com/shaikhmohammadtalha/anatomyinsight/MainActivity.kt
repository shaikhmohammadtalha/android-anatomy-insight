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
package com.shaikhmohammadtalha.anatomyinsight

import android.os.Bundle
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shaikhmohammadtalha.anatomyinsight.ui.theme.AnatomyInsightTheme
import com.google.android.filament.utils.Utils

class MainActivity : ComponentActivity() {

    companion object {
        init {
            Utils.init() // Initialize Filament
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnatomyInsightTheme {
                MainActivityContent()
            }
        }
    }
}

@Composable
fun MainActivityContent() {
    val renderer = remember { ModelRenderer() }
    var currentModel by remember { mutableStateOf<AnatomyModel?>(null) }
    var subParts by remember { mutableStateOf<List<AnatomyModel>?>(null) } // Null means show main models
    var showMainModels by remember { mutableStateOf(true) } // Controls list switching

    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
    // 🔹 Create a ScrollState to control scrolling
    val listState = rememberLazyListState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Use themed background color
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Model Display Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f) // Adjust height proportionally
                    .background(color = Color.Gray)
            ) {
                if (currentModel != null) {
                    println("Displaying model: ${currentModel?.name}") // Debug log
                    AndroidView(factory = { context ->
                        SurfaceView(context).apply {
                            renderer.onSurfaceAvailable(this, lifecycleOwner.value.lifecycle)
                        }
                    })
                } else {
                    println("No model selected") // Debug log
                }
            }

            // 🔹 Wrap ModelRows in a Box to ensure stacking order
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
            ) {
                // 🔹 Reset scroll when switching between main models & subparts
                LaunchedEffect(showMainModels) {
                    listState.scrollToItem(0) // Ensure list starts at the top
                }

                // Show either main models or subparts
                ModelRows(
                    models = if (showMainModels) models else subParts ?: emptyList(),
                    onModelSelect = { model ->
                        if (showMainModels) {
                            // Selecting a main model
                            currentModel = model
                            subParts = fetchSubParts(model.name)
                            showMainModels = false
                            renderer.loadModel(model.filePath)
                        } else {
                            // Selecting a subpart (does not change list)
                            renderer.loadModel(model.filePath)
                        }
                    },
                    currentModel = currentModel,
                    listState = listState // Pass scroll state
                )

                // 🔹 Fix button disappearing issue
                if (subParts != null) {
                    Button(
                        onClick = {
                            showMainModels = !showMainModels
                            subParts = if (showMainModels) null else subParts // Reset if going back
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter) // Ensure it's visible
                    ) {
                        Text(if (showMainModels) "View Subparts" else "Back to Models")
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MainActivityPreview() {
    AnatomyInsightTheme {
        MainActivityContent()
    }
}
