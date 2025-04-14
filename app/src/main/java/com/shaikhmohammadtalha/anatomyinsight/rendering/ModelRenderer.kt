package com.shaikhmohammadtalha.anatomyinsight.rendering

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import android.view.Choreographer
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

/**
 * Manages the rendering of 3D models using Filament's `ModelViewer`.
 * Handles surface initialization, lifecycle management, model loading, and rendering.
 */
class ModelRenderer {

    // Core rendering and lifecycle components
    private lateinit var surfaceView: SurfaceView
    private lateinit var lifecycle: Lifecycle
    private lateinit var choreographer: Choreographer
    private lateinit var uiHelper: UiHelper
    private lateinit var modelViewer: ModelViewer

    // Internal state flags
    private var surfaceInitialized = false
    private var pendingModelPath: String? = null

    // Access to asset manager for reading model and environment files
    private val assets: AssetManager
        get() = surfaceView.context.assets

    // Handles frame callbacks to trigger continuous rendering
    private val frameScheduler = FrameCallback()

    // Observes lifecycle to pause/resume rendering appropriately
    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            choreographer.postFrameCallback(frameScheduler)
        }

        override fun onPause(owner: LifecycleOwner) {
            choreographer.removeFrameCallback(frameScheduler)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            choreographer.removeFrameCallback(frameScheduler)
            lifecycle.removeObserver(this)
        }
    }

    /**
     * Loads a 3D model from assets and renders it.
     * If the surface is not ready, defers loading until it becomes available.
     * @param filePath The path to the model file (GLB format).
     */
    fun loadModel(filePath: String) {
        if (!surfaceInitialized) {
            println("Surface not initialized. Pending model: $filePath")
            pendingModelPath = filePath
            return
        }

        println("Loading model from: $filePath") // Debug log

        try {
            // Read model file into a ByteBuffer
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

                    // Load model into ModelViewer
                    modelViewer.loadModelGlb(byteBuffer)
                    modelViewer.transformToUnitCube()
                    println("Model loaded successfully: $filePath")
                }
            }
        } catch (e: Exception) {
            println("Error loading model: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Initializes rendering when the SurfaceView becomes available.
     *
     * @param surfaceView The `SurfaceView` used for rendering.
     * @param lifecycle The lifecycle of the associated activity/fragment.
     */
    @SuppressLint("ClickableViewAccessibility")
    fun onSurfaceAvailable(surfaceView: SurfaceView, lifecycle: Lifecycle, context: Context) {
        println("SurfaceView is available")
        this.surfaceView = surfaceView
        this.lifecycle = lifecycle

        choreographer = Choreographer.getInstance()
        lifecycle.addObserver(lifecycleObserver)

        // Prepare UI helper for setting up the rendering surface
        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
            isOpaque = false
        }

        // Initialize the model viewer with rendering surface and UI handling
        modelViewer = ModelViewer(surfaceView = surfaceView, uiHelper = uiHelper)
        surfaceInitialized = true
        println("Renderer initialized successfully!")

        // Handle touch events for model interaction
        surfaceView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                    modelViewer.onTouchEvent(event)
                }
            }
            true
        }

        // Apply rendering quality settings based on user preferences
        CoroutineScope(Dispatchers.IO).launch {
            GraphicsSettingsStore.settingsFlow(context).first().let { settings ->
                val qualityLevel = when (settings.quality) {
                    "Low" -> View.QualityLevel.LOW
                    "Medium" -> View.QualityLevel.MEDIUM
                    "High" -> View.QualityLevel.HIGH
                    "Ultra" -> View.QualityLevel.ULTRA
                    else -> View.QualityLevel.MEDIUM
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

        modelViewer.scene.skybox = null

        // Load the HDR environment for lighting
        createEnvironment(
            assets, modelViewer.engine, "environments/lightroom_14b.hdr", modelViewer.scene
        )

        // Remove skybox, keeping only HDR lighting
        // Remove below line to render the HDR environment
        modelViewer.scene.skybox = null

        // Add additional lighting sources
        createLights(modelViewer.engine, modelViewer.scene)

        // Set blending mode for transparency
        modelViewer.view.blendMode = View.BlendMode.TRANSLUCENT

        // Ensure the renderer clears the screen before each frame
        modelViewer.renderer.clearOptions = modelViewer.renderer.clearOptions.apply {
            clear = true
        }

        // Load pending model if any
        pendingModelPath?.let {
            println("Loading pending model: $it")
            loadModel(it)
            pendingModelPath = null
        }

        // Force an initial frame render
        surfaceView.post {
            println("Forcing a frame render")
            choreographer.postFrameCallback(frameScheduler)
        }

        Log.d("ModelRenderer", "Renderer initialized successfully.")
    }

    /**
     * Handles frame rendering at regular intervals.
     */
    private inner class FrameCallback : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            modelViewer.render(frameTimeNanos)
            choreographer.postFrameCallback(this)
        }
    }
}