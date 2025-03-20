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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.filament.utils.Utils
import com.shaikhmohammadtalha.anatomyinsight.ui.theme.AnatomyInsightTheme

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
    var selectedSubpart by remember { mutableStateOf<AnatomyModel?>(null) } // Store selected subpart globally

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
                    .weight(0.60f) // Adjust height proportionally
                    .background(MaterialTheme.colorScheme.secondary) // Now using AMOLED Black
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
                    .weight(0.40f)
                    .background(MaterialTheme.colorScheme.background) // Apply theme surface
            ) {
                // 🔹 Reset scroll when switching between main models & subparts
                LaunchedEffect(showMainModels) {
                    listState.scrollToItem(0) // Ensure list starts at the top
                }

                // Show either main models or subparts
                if (showMainModels) {
                    ModelRows(
                        models = models,
                        onModelSelect = { model ->
                            currentModel = model
                            subParts = fetchSubParts(model.name)
                            showMainModels = false
                            renderer.loadModel(model.filePath)
                        },
                        currentModel = currentModel,
                        listState = listState,
                        showMainModels = showMainModels,
                        toggleMainModels = { showMainModels = !showMainModels }
                    )
                } else {
                    SubpartRows(
                        subparts = fetchSubParts(currentModel?.name ?: ""),
                        onSubpartSelect = { subpart -> // Renamed to `subpart` to avoid conflict
                            selectedSubpart = subpart
                            renderer.loadModel(subpart.filePath)
                        },
                        currentModel = currentModel,
                        selectedSubpart = selectedSubpart,
                        showMainModels = showMainModels,
                        toggleMainModels = { showMainModels = !showMainModels }
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MainActivityPreview() {
    AnatomyInsightTheme(darkTheme = true) {
        MainActivityContent()
    }
}
