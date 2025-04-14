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
import com.shaikhmohammadtalha.anatomyinsight.datastore.AppTheme
import com.shaikhmohammadtalha.anatomyinsight.ui.components.BottomNavBar
import com.shaikhmohammadtalha.anatomyinsight.ui.screens.CreditsScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.utils.DoubleBackToExitHandler
import com.shaikhmohammadtalha.anatomyinsight.ui.screens.GraphicsSettingsScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.screens.PrivacyPolicyScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.screens.SearchScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.screens.SettingsScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.screens.ThemeSettingsScreen
import com.shaikhmohammadtalha.anatomyinsight.datastore.ThemeStore
import com.shaikhmohammadtalha.anatomyinsight.rendering.ModelRenderer
import com.shaikhmohammadtalha.anatomyinsight.ui.components.ModelRows
import com.shaikhmohammadtalha.anatomyinsight.ui.components.SubpartRows
import com.shaikhmohammadtalha.anatomyinsight.ui.model.AnatomyModel
import com.shaikhmohammadtalha.anatomyinsight.ui.theme.AnatomyInsightTheme
import com.shaikhmohammadtalha.anatomyinsight.viewmodel.ModelViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.collections.find

/**
 * Main entry point of the AnatomyInsight app.
 * Sets up theming, initializes Filament, and defines navigation routes.
 */
class MainActivity : ComponentActivity() {

    companion object {
        init {
            Utils.init() // Initialize Filament once for the app lifecycle
        }
    }

    private val modelViewModel: ModelViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        setContent {
            val navController = rememberNavController()
            val theme by ThemeStore.themeFlow(this).collectAsState(initial = AppTheme.AUTO)

            AnatomyInsightTheme(appTheme = theme) {
                NavHost(navController, startDestination = "main") {
                    composable("main") {
                        MainActivityContent(modelViewModel, navController)
                    }
                    composable("search") {
                        SearchScreen(navController, modelViewModel)
                    }
                    composable("settings") {
                        SettingsScreen(navController)
                    }
                    composable("graphics_settings") {
                        GraphicsSettingsScreen(navController)
                    }
                    composable("theme_settings") {
                        ThemeSettingsScreen(navController)
                    }
                    composable("privacy_policy") {
                        PrivacyPolicyScreen(navController)
                    }
                    composable("credits") {
                        CreditsScreen(navController)
                    }
                }
            }
        }
    }
}

/**
 * Main screen layout that handles the 3D viewer, model list, and subpart list.
 * This composable also manages navigation state, model loading, and renderer interaction.
 *
 * @param modelViewModel ViewModel providing model and subpart data
 * @param navController Navigation controller for handling in-app routes
 */
@Composable
fun MainActivityContent(modelViewModel: ModelViewModel, navController: NavHostController) {
    // Handles double back press behavior
    DoubleBackToExitHandler(navController)

    // Renderer responsible for displaying 3D models
    val renderer = remember { ModelRenderer() }

    // UI state: currently selected model and subpart
    var currentModel by remember { mutableStateOf<AnatomyModel?>(null) }
    var showMainModels by remember { mutableStateOf(true) }
    var selectedSubpart by remember { mutableStateOf<AnatomyModel?>(null) }

    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    // Scroll state for model and subpart lists
    val modelListState = rememberLazyListState()
    val subpartListState = rememberLazyListState()

    // Observe model data from the database
    val modelEntities by modelViewModel.allModels.collectAsState(initial = emptyList())

    // Transform database entities into UI-friendly models
    val models = modelEntities.map { entity ->
        AnatomyModel(name = entity.name, filePath = entity.filePath)
    }

    // Dynamically load subparts when a model is selected
    val subParts by produceState<List<AnatomyModel>>(initialValue = emptyList(), currentModel) {
        currentModel?.let { model ->
            modelViewModel.fetchSubParts(model.name).collect { value = it }
        }
    }

    var isSearchMode by remember { mutableStateOf(false) }

    /**
     * When returning from SearchScreen with a selected subpart,
     * this block sets the corresponding model and subpart state,
     * ensuring the correct view and model are rendered.
     */
    LaunchedEffect(navController.currentBackStackEntry) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        val selectedSubpartId: Int? = savedStateHandle?.get("selectedSubpartId")

        if (selectedSubpartId != null) {
            modelViewModel.getSubpartById(selectedSubpartId).collectLatest { subpartEntity ->
                subpartEntity?.let { subpart ->
                    modelViewModel.getModelById(subpart.modelId).collectLatest { modelEntity ->
                        modelEntity?.let { model ->
                            showMainModels = false
                            if (currentModel?.name != model.name) {
                                currentModel = AnatomyModel(
                                    name = model.name, filePath = model.filePath
                                )
                            }

                            modelViewModel.fetchSubParts(model.name).collectLatest { subparts ->
                                val matchedSubpart = subparts.find { it.name == subpart.name }
                                matchedSubpart?.let {
                                    selectedSubpart = it
                                    renderer.loadModel(it.filePath)
                                }
                            }
                        }
                    }
                }
            }
            savedStateHandle.remove<Int>("selectedSubpartId")
        }
    }

    // Main scaffold with bottom navigation and model/subpart display
    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
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

            // Top half: displays the current 3D model
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.50f)
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                if (currentModel != null) {
                    println("Displaying model: ${currentModel?.name}") // Debug log
                    AndroidView(factory = { context ->
                        SurfaceView(context).apply {
                            renderer.onSurfaceAvailable(this, lifecycleOwner.value.lifecycle, context)
                        }
                    })
                } else {
                    println("No model selected") // Debug log
                }
            }

            // Bottom half: displays either the model list or subpart list
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.50f)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 8.dp)
            ) {
                val scope = rememberCoroutineScope()

                if (showMainModels) {
                    ModelRows(
                        models = models,
                        viewModel = modelViewModel,
                        onModelSelect = { model ->
                            scope.launch { subpartListState.scrollToItem(0) }
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
                        listState = subpartListState
                    )
                }
            }
        }
    }
}