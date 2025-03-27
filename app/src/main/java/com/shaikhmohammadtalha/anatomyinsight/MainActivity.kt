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
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.filament.utils.Utils
import com.shaikhmohammadtalha.anatomyinsight.ui.theme.AnatomyInsightTheme
import com.shaikhmohammadtalha.viewmodel.ModelViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        init {
            Utils.init() // Initialize Filament
        }
    }

    private val modelViewModel: ModelViewModel by viewModels() // ✅ Added

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnatomyInsightTheme {
                MainActivityContent(modelViewModel)
            }
        }
    }
}

@Composable
fun MainActivityContent(modelViewModel: ModelViewModel) {
    val renderer = remember { ModelRenderer() }
    var currentModel by remember { mutableStateOf<AnatomyModel?>(null) }
    var showMainModels by remember { mutableStateOf(true) } // Controls whether the main model list or subparts are displayed
    var selectedSubpart by remember { mutableStateOf<AnatomyModel?>(null) } // Stores the selected subpart globally

    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    // Remember scroll states for model and subpart lists to retain their positions
    val modelListState = rememberLazyListState()
    val subpartListState = rememberLazyListState()

    // Collect the list of all models from ViewModel
    val modelEntities by modelViewModel.allModels.collectAsState(initial = emptyList())

    // Convert model entities into UI models
    val models = modelEntities.map { entity ->
        AnatomyModel(name = entity.name, filePath = entity.filePath)
    }

    // Fetch subparts dynamically when a model is selected
    val subParts by produceState<List<AnatomyModel>>(initialValue = emptyList(), currentModel) {
        currentModel?.let { model ->
            modelViewModel.fetchSubParts(model.name).collect { value = it }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Apply theme-based background color
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Model Display Section (Top 60% of the screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.60f)
                    .background(MaterialTheme.colorScheme.secondary) // Background color for model display
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

            // Model List Section (Bottom 40% of the screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.40f)
                    .background(MaterialTheme.colorScheme.background) // Ensure consistent background
            ) {
                val scope = rememberCoroutineScope()

                // Toggle between main model list and subpart list
                if (showMainModels) {
                    ModelRows(
                        models = models, // Pass computed models instead of directly using ViewModel
                        viewModel = modelViewModel,
                        onModelSelect = { model ->
                            // Reset subpart scroll state when a new model is selected
                            scope.launch {
                                subpartListState.scrollToItem(0)
                            }
                            currentModel = model
                            showMainModels = false
                            renderer.loadModel(model.filePath)
                        },
                        currentModel = currentModel,
                        showMainModels = showMainModels,
                        toggleMainModels = { showMainModels = !showMainModels },
                        listState = modelListState
                    )
                } else {
                    SubpartRows(
                        subparts = subParts,
                        onSubpartSelect = { subpart ->
                            selectedSubpart = subpart
                            renderer.loadModel(subpart.filePath)
                        },
                        currentModel = currentModel,
                        selectedSubpart = selectedSubpart,
                        showMainModels = showMainModels,
                        toggleMainModels = { showMainModels = !showMainModels },
                        viewModel = modelViewModel,
                        listState = subpartListState // Maintain subpart scroll state
                    )
                }
            }
        }
    }
}

