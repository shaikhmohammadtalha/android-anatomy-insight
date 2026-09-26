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
package com.shaikhmohammadtalha.anatomyinsight.rendering

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.filament.View
import com.google.android.filament.android.UiHelper
import com.google.android.filament.utils.ModelViewer
import com.shaikhmohammadtalha.anatomyinsight.datastore.GraphicsSettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val TAG = "ModelRenderer"

/**
 * Manages the rendering of 3D models using Filament's [ModelViewer].
 * Handles surface initialisation, lifecycle management, model loading, and frame rendering.
 */
class ModelRenderer {

    private lateinit var surfaceView: SurfaceView
    private lateinit var lifecycle: Lifecycle
    private lateinit var choreographer: android.view.Choreographer
    private lateinit var uiHelper: UiHelper
    private lateinit var modelViewer: ModelViewer

    @Volatile
    private var surfaceInitialized = false
    @Volatile
    private var isModelLoaded = false
    private var pendingModelPath: String? = null
    var currentModelPath: String? = null
        private set

    private val assets: AssetManager
        get() = surfaceView.context.assets

    private val frameScheduler = FrameCallback()

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            if (surfaceInitialized) {
                choreographer.postFrameCallback(frameScheduler)
            }
        }

        override fun onPause(owner: LifecycleOwner) {
            choreographer.removeFrameCallback(frameScheduler)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            destroy()
        }
    }

    /**
     * Cleans up the renderer, stopping Choreographer callbacks and detaching lifecycle observers.
     */
    fun destroy() {
        if (this::choreographer.isInitialized) {
            choreographer.removeFrameCallback(frameScheduler)
        }
        if (this::lifecycle.isInitialized) {
            lifecycle.removeObserver(lifecycleObserver)
        }
        surfaceInitialized = false
        isModelLoaded = false
    }

    /**
     * Loads a 3D GLB model from the app's assets.
     * If the rendering surface is not yet ready, the load is deferred until it becomes available.
     *
     * @param filePath The asset-relative path to the GLB file (e.g. "models/Myology/Biceps.glb")
     */
    fun loadModel(filePath: String) {
        currentModelPath = filePath
        if (!surfaceInitialized) {
            pendingModelPath = filePath
            return
        }

        Log.d(TAG, "Loading model: $filePath")

        try {
            assets.open(filePath).use { input ->
                ByteArrayOutputStream().use { output ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    val bytes = output.toByteArray()
                    val byteBuffer = ByteBuffer.allocateDirect(bytes.size).apply {
                        order(ByteOrder.nativeOrder())
                        put(bytes)
                        rewind()
                    }
                    modelViewer.loadModelGlb(byteBuffer)
                    modelViewer.transformToUnitCube()
                    isModelLoaded = true
                    Log.d(TAG, "Model loaded successfully: $filePath")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model: ${e.message}", e)
        }
    }

    /**
     * Initialises the Filament renderer when the [SurfaceView] becomes available.
     * Sets up the model viewer, lifecycle observer, touch handling, and graphics settings.
     *
     * @param surfaceView The [SurfaceView] used for rendering
     * @param lifecycle The lifecycle of the associated activity
     * @param context Android context for asset and DataStore access
     */
    @SuppressLint("ClickableViewAccessibility")
    fun onSurfaceAvailable(surfaceView: SurfaceView, lifecycle: Lifecycle, context: Context) {
        if (surfaceInitialized && this@ModelRenderer::surfaceView.isInitialized && this.surfaceView == surfaceView) {
            return
        }

        // Clean up previous registration if re-initialising
        if (this::choreographer.isInitialized) {
            choreographer.removeFrameCallback(frameScheduler)
        }
        if (this::lifecycle.isInitialized) {
            this.lifecycle.removeObserver(lifecycleObserver)
        }

        this.surfaceView = surfaceView
        this.lifecycle = lifecycle

        choreographer = android.view.Choreographer.getInstance()
        lifecycle.addObserver(lifecycleObserver)

        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
            isOpaque = false
        }

        modelViewer = ModelViewer(surfaceView = surfaceView, uiHelper = uiHelper)
        surfaceInitialized = true

        surfaceView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE,
                MotionEvent.ACTION_UP -> modelViewer.onTouchEvent(event)
            }
            true
        }

        // Apply stored graphics settings
        CoroutineScope(Dispatchers.IO).launch {
            GraphicsSettingsStore.settingsFlow(context).first().let { settings ->
                val qualityLevel = when (settings.quality) {
                    "LOW" -> View.QualityLevel.LOW
                    "MEDIUM" -> View.QualityLevel.MEDIUM
                    "HIGH" -> View.QualityLevel.HIGH
                    "ULTRA" -> View.QualityLevel.ULTRA
                    else -> View.QualityLevel.LOW
                }
                withContext(Dispatchers.Main) {
                    modelViewer.view.apply {
                        renderQuality = renderQuality.apply { hdrColorBuffer = qualityLevel }
                        multiSampleAntiAliasingOptions =
                            multiSampleAntiAliasingOptions.apply { enabled = settings.msaa }
                        ambientOcclusionOptions =
                            ambientOcclusionOptions.apply { enabled = settings.ao }
                        bloomOptions = bloomOptions.apply { enabled = settings.bloom }
                    }
                }
            }
        }

        // Transparent background (no skybox)
        modelViewer.scene.skybox = null

        // HDR lighting
        createEnvironment(
            assets, modelViewer.engine, "environments/lightroom_14b.hdr", modelViewer.scene
        )
        modelViewer.scene.skybox = null

        createLights(modelViewer.engine, modelViewer.scene)

        modelViewer.view.blendMode = View.BlendMode.TRANSLUCENT
        modelViewer.renderer.clearOptions = modelViewer.renderer.clearOptions.apply {
            clear = true
        }

        // Load any model that was requested before the surface was ready, or restore current model
        val modelToLoad = pendingModelPath ?: currentModelPath
        modelToLoad?.let {
            loadModel(it)
            pendingModelPath = null
        }

        surfaceView.post { choreographer.postFrameCallback(frameScheduler) }
        Log.d(TAG, "Renderer initialised successfully.")
    }

    private inner class FrameCallback : android.view.Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (surfaceInitialized && isModelLoaded && this@ModelRenderer::surfaceView.isInitialized && surfaceView.width > 0 && surfaceView.height > 0 && this@ModelRenderer::uiHelper.isInitialized && uiHelper.isReadyToRender) {
                try {
                    modelViewer.render(frameTimeNanos)
                } catch (e: Exception) {
                    Log.e(TAG, "Render frame error: ${e.message}", e)
                }
            }
            if (surfaceInitialized && this@ModelRenderer::choreographer.isInitialized) {
                choreographer.postFrameCallback(this)
            }
        }
    }
}
