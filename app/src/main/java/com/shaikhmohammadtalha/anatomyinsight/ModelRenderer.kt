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

import android.annotation.SuppressLint
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
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ModelRenderer {
    private lateinit var surfaceView: SurfaceView
    private lateinit var lifecycle: Lifecycle
    private lateinit var choreographer: Choreographer
    private lateinit var uiHelper: UiHelper
    private lateinit var modelViewer: ModelViewer
    private var surfaceInitialized = false
    private var pendingModelPath: String? = null

    private val assets: AssetManager
        get() = surfaceView.context.assets
    private val frameScheduler = FrameCallback()

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

    fun loadModel(filePath: String) {
        if (!surfaceInitialized) {
            println("Surface not initialized. Pending model: $filePath")
            pendingModelPath = filePath
            return
        }

        println("Loading model from: $filePath") // Debug log

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
                    println("Model loaded successfully: $filePath")
                }
            }
        } catch (e: Exception) {
            println("Error loading model: ${e.message}")
            e.printStackTrace()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    fun onSurfaceAvailable(surfaceView: SurfaceView, lifecycle: Lifecycle) {
        println("SurfaceView is available")
        this.surfaceView = surfaceView
        this.lifecycle = lifecycle

        choreographer = Choreographer.getInstance()
        lifecycle.addObserver(lifecycleObserver)

        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
            isOpaque = false
        }

        modelViewer = ModelViewer(surfaceView = surfaceView, uiHelper = uiHelper)
        surfaceInitialized = true
        println("Renderer initialized successfully!")
        surfaceView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                    modelViewer.onTouchEvent(event)
                }
            }
            true
        }

        modelViewer.view.apply {
            renderQuality = renderQuality.apply { hdrColorBuffer = View.QualityLevel.LOW }
            multiSampleAntiAliasingOptions = multiSampleAntiAliasingOptions.apply { enabled = true }
            ambientOcclusionOptions = ambientOcclusionOptions.apply { enabled = true }
            bloomOptions = bloomOptions.apply { enabled = true }
        }
        modelViewer.scene.skybox = null
        // Load the HDR environment using the utility function
        createEnvironment(assets, modelViewer.engine, "environments/lightroom_14b.hdr", modelViewer.scene)

        // Add additional lights using the utility function
        createLights(modelViewer.engine, modelViewer.scene)

        modelViewer.view.blendMode = View.BlendMode.TRANSLUCENT
        modelViewer.renderer.clearOptions = modelViewer.renderer.clearOptions.apply {
            clear = true
        }


        pendingModelPath?.let {
            println("Loading pending model: $it")
            loadModel(it)
            pendingModelPath = null
        }
        // 🔹 Force re-render after initialization
        surfaceView.post {
            println("Forcing a frame render")
            choreographer.postFrameCallback(frameScheduler)
        }

        Log.d("ModelRenderer", "Renderer initialized successfully.")
    }

    private inner class FrameCallback : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            modelViewer.render(frameTimeNanos)
            choreographer.postFrameCallback(this)
        }
    }
}
