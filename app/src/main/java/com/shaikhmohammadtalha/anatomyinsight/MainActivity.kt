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

import android.os.Build
import android.os.Bundle
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.filament.utils.Utils
import com.shaikhmohammadtalha.anatomyinsight.ui.BottomNavBar
import com.shaikhmohammadtalha.anatomyinsight.ui.DoubleBackToExitHandler
import com.shaikhmohammadtalha.anatomyinsight.ui.SearchScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.SettingsScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.theme.AnatomyInsightTheme
import com.shaikhmohammadtalha.viewmodel.ModelViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        init {
            Utils.init() // Initialize Filament
        }
    }
    private val modelViewModel: ModelViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        setContent {
            val navController = rememberNavController() // Navigation controller

            AnatomyInsightTheme {
                NavHost(navController, startDestination = "main") {
                    composable("main") {
                        MainActivityContent(modelViewModel, navController)
                    }
                    composable("search") {
                        SearchScreen(
                            navController = navController,
                            modelViewModel = modelViewModel
                        )
                    }

                    composable("settings") {
                        SettingsScreen(navController)
                    }
                }
            }
        }
    }
}

@Composable
fun MainActivityContent(modelViewModel: ModelViewModel, navController: NavHostController) {
    DoubleBackToExitHandler(navController)


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

    var isSearchMode by remember { mutableStateOf(false) }


    LaunchedEffect(navController.currentBackStackEntry) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        val selectedSubpartId: Int? = savedStateHandle?.get("selectedSubpartId")

        if (selectedSubpartId != null) {
            // Fetch the selected subpart details
            modelViewModel.getSubpartById(selectedSubpartId).collectLatest { subpartEntity ->
                subpartEntity?.let { subpart ->
                    // Fetch the corresponding model using the subpart's model ID
                    modelViewModel.getModelById(subpart.modelId).collectLatest { modelEntity ->
                        modelEntity?.let { model ->
                            // Set the current model only if it's not already set
                            if (currentModel?.name != model.name) {
                                currentModel = AnatomyModel(
                                    name = model.name,
                                    filePath = model.filePath
                                )
                            }

                            // ✅ Fetch the correct subpart model file path
                            modelViewModel.fetchSubParts(model.name).collectLatest { subparts ->
                                val matchedSubpart = subparts.find { it.name == subpart.name }
                                matchedSubpart?.let {
                                    selectedSubpart = it

                                    // ✅ Load the correct subpart model
                                    renderer.loadModel(it.filePath)
                                }
                            }
                            // Show the subpart details
                            showMainModels = false
                        }
                    }
                }
            }
            // Clear saved state to avoid re-triggering effect
            savedStateHandle.remove<Int>("selectedSubpartId")
        }
    }


    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                // Pass drawerState
                onSearchClick = {
                    isSearchMode = true
                    navController.navigate("search")
                }
            )

        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)

        ) {

            // Model Display Section (Top 50% of the screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.50f)
                    .background(MaterialTheme.colorScheme.secondary) // Background color for model display
            ) {
                if (currentModel != null) {
                    println("Displaying model: ${currentModel?.name}") // Debug log
                    AndroidView(factory = { context ->
                        SurfaceView(context).apply {
                            renderer.onSurfaceAvailable(
                                this,
                                lifecycleOwner.value.lifecycle
                            )
                        }
                    })
                } else {
                    println("No model selected") // Debug log
                }
            }

            // Model List Section (Bottom 35% of the screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.50f)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 8.dp)// Ensure consistent background
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

