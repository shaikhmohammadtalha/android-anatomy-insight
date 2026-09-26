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

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.filament.utils.Utils
import com.shaikhmohammadtalha.anatomyinsight.datastore.AppTheme
import com.shaikhmohammadtalha.anatomyinsight.datastore.ThemeStore
import com.shaikhmohammadtalha.anatomyinsight.rendering.ModelRenderer
import com.shaikhmohammadtalha.anatomyinsight.ui.components.AppNavigationRail
import com.shaikhmohammadtalha.anatomyinsight.ui.components.BottomNavBar
import com.shaikhmohammadtalha.anatomyinsight.ui.components.ModelRows
import com.shaikhmohammadtalha.anatomyinsight.ui.components.SubpartRows
import com.shaikhmohammadtalha.anatomyinsight.ui.model.AnatomyModel
import com.shaikhmohammadtalha.anatomyinsight.ui.screens.CreditsScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.screens.GraphicsSettingsScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.screens.HistoryScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.screens.PrivacyPolicyScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.screens.SearchScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.screens.SettingsScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.screens.ThemeSettingsScreen
import com.shaikhmohammadtalha.anatomyinsight.ui.theme.AnatomyInsightTheme
import com.shaikhmohammadtalha.anatomyinsight.ui.utils.DoubleBackToExitHandler
import com.shaikhmohammadtalha.anatomyinsight.viewmodel.ModelViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Main entry point of the AnatomyInsight app.
 * Sets up theming, initialises Filament, and defines all navigation routes.
 */
class MainActivity : ComponentActivity() {

    companion object {
        init {
            Utils.init() // Initialise Filament once for the app lifecycle
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
            val renderer = remember { ModelRenderer() }

            AnatomyInsightTheme(appTheme = theme) {
                NavHost(
                    navController = navController,
                    startDestination = "main",
                    enterTransition = { fadeIn(animationSpec = tween(220)) },
                    exitTransition = { fadeOut(animationSpec = tween(200)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(220)) },
                    popExitTransition = { fadeOut(animationSpec = tween(200)) }
                ) {
                    composable("main") {
                        MainActivityContent(modelViewModel, navController, renderer)
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
                    composable("history") {
                        HistoryScreen(navController, modelViewModel)
                    }
                }
            }
        }
    }
}

/**
 * Main screen layout: top bar with dynamic model/subpart title, 3D viewport,
 * and scrollable model/subpart panel below (or side-by-side on wide screens).
 * Supports fullscreen for both the 3D viewport and the content panel.
 *
 * @param modelViewModel ViewModel providing model and subpart data
 * @param navController Navigation controller for handling in-app routes
 * @param renderer Shared 3D Filament renderer instance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    modelViewModel: ModelViewModel,
    navController: NavHostController,
    renderer: ModelRenderer
) {
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600 ||
            configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var currentModel by remember { mutableStateOf<AnatomyModel?>(null) }
    var showMainModels by remember { mutableStateOf(true) }
    var selectedSubpart by remember { mutableStateOf<AnatomyModel?>(null) }

    // Fullscreen state: null = split, "viewport" = 3D full, "panel" = panel full
    var fullscreenMode by remember { mutableStateOf<String?>(null) }

    // Animate viewport and panel weights
    val viewportWeight by animateFloatAsState(
        targetValue = when (fullscreenMode) {
            "viewport" -> 1f
            "panel"    -> 0f
            else       -> 0.50f
        },
        animationSpec = tween(300),
        label = "viewportWeight"
    )
    val panelWeight by animateFloatAsState(
        targetValue = when (fullscreenMode) {
            "panel"    -> 1f
            "viewport" -> 0f
            else       -> 0.50f
        },
        animationSpec = tween(300),
        label = "panelWeight"
    )

    // Back handler: collapse fullscreen before navigating back
    BackHandler(enabled = fullscreenMode != null) {
        fullscreenMode = null
    }
    BackHandler(enabled = fullscreenMode == null && !showMainModels) {
        showMainModels = true
    }

    if (showMainModels && fullscreenMode == null) {
        DoubleBackToExitHandler(navController)
    }

    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
    val modelListState = rememberLazyListState()
    val subpartListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val modelEntities by modelViewModel.allModels.collectAsState(initial = emptyList())
    val models = modelEntities.map { entity ->
        AnatomyModel(name = entity.name, filePath = entity.filePath)
    }

    val subParts by produceState<List<AnatomyModel>>(initialValue = emptyList(), currentModel) {
        currentModel?.let { model ->
            modelViewModel.fetchSubParts(model.name).collect { value = it }
        }
    }

    // Dynamic Top badge title
    val topBarTitle = when {
        selectedSubpart != null && !showMainModels -> selectedSubpart!!.name.replace("_", " ")
        currentModel != null -> currentModel!!.name
        else -> "Anatomy Insight"
    }

    // When returning from SearchScreen or HistoryScreen with a selected subpart ID
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
                                    modelViewModel.recordHistory(
                                        subpartId = subpart.id,
                                        subpartName = subpart.name,
                                        modelName = model.name
                                    )
                                }
                            }
                        }
                    }
                }
            }
            savedStateHandle.remove<Int>("selectedSubpartId")
        }
    }

    // ── 3D Viewport Composable ──────────────────────────────────────────────
    val viewportContent = @Composable { modifier: Modifier ->
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            if (currentModel != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        SurfaceView(context).apply {
                            renderer.onSurfaceAvailable(
                                this, lifecycleOwner.value.lifecycle, context
                            )
                        }
                    },
                    update = { surfaceView ->
                        renderer.onSurfaceAvailable(
                            surfaceView, lifecycleOwner.value.lifecycle, surfaceView.context
                        )
                    }
                )
                // Title badge (top centre)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = topBarTitle,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
                // Fullscreen toggle button (top-right corner)
                IconButton(
                    onClick = {
                        fullscreenMode = if (fullscreenMode == "viewport") null else "viewport"
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (fullscreenMode == "viewport")
                            Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (fullscreenMode == "viewport")
                            "Exit fullscreen" else "Fullscreen 3D view",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.ViewInAr,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.4f)
                    )
                    Text(
                        "Tap a model below to begin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }

    // ── Content Panel Composable (Models / Subparts) ────────────────────────
    val panelContent = @Composable { modifier: Modifier ->
        Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
            if (showMainModels) {
                ModelRows(
                    models = models,
                    viewModel = modelViewModel,
                    onModelSelect = { model ->
                        if (currentModel?.name == model.name) {
                            scope.launch { subpartListState.scrollToItem(0) }
                            showMainModels = false
                        } else {
                            currentModel = model
                            selectedSubpart = null
                            renderer.loadModel(model.filePath)
                        }
                    },
                    currentModel = currentModel,
                    showMainModels = showMainModels,
                    toggleMainModels = {
                        scope.launch { subpartListState.scrollToItem(0) }
                        showMainModels = !showMainModels
                    },
                    listState = modelListState,
                    fullscreen = fullscreenMode == "panel",
                    onToggleFullscreen = {
                        fullscreenMode = if (fullscreenMode == "panel") null else "panel"
                    }
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
                    listState = subpartListState,
                    onHistoryRecord = { subpartId, name, modelName ->
                        modelViewModel.recordHistory(subpartId, name, modelName)
                    },
                    fullscreen = fullscreenMode == "panel",
                    onToggleFullscreen = {
                        fullscreenMode = if (fullscreenMode == "panel") null else "panel"
                    }
                )
            }
        }
    }

    // ── Responsive Layout Scaffold ──────────────────────────────────────────
    if (isWideScreen) {
        Row(modifier = Modifier.fillMaxSize()) {
            AppNavigationRail(navController = navController, activeTab = "main")
            if (fullscreenMode != "panel") {
                viewportContent(
                    Modifier
                        .fillMaxHeight()
                        .weight(if (fullscreenMode == "viewport") 1f else 0.55f)
                )
            }
            if (fullscreenMode != "viewport") {
                panelContent(
                    Modifier
                        .fillMaxHeight()
                        .weight(if (fullscreenMode == "panel") 1f else 0.45f)
                )
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                if (fullscreenMode == null) {
                    BottomNavBar(navController = navController, activeTab = "main")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (viewportWeight > 0f) {
                    viewportContent(
                        Modifier
                            .fillMaxWidth()
                            .weight(viewportWeight.coerceAtLeast(0.001f))
                    )
                }
                if (panelWeight > 0f) {
                    panelContent(
                        Modifier
                            .fillMaxWidth()
                            .weight(panelWeight.coerceAtLeast(0.001f))
                    )
                }
            }
        }
    }
}
