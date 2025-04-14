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

import android.content.res.AssetManager
import android.util.Log
import com.google.android.filament.Engine
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.EntityManager
import com.google.android.filament.Scene
import com.google.android.filament.Skybox
import com.google.android.filament.utils.HDRLoader
import com.google.android.filament.utils.IBLPrefilterContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Creates a directional light and adds it to the scene.
 *
 * @param engine The Filament engine instance.
 * @param scene The scene where the light will be added.
 */

fun createLights(engine: Engine, scene: Scene) {
    val entityManager = EntityManager.get()
    val lightEntity = entityManager.create()

    // Create a directional light
    LightManager.Builder(LightManager.Type.DIRECTIONAL).color(1.0f, 1.0f, 1.0f) // White light
        .intensity(50_000.0f) // Adjust intensity as needed
        .direction(0.0f, -1.0f, 0.0f) // Direction pointing downwards
        .castShadows(true) // Enable shadows
        .build(engine, lightEntity)

    // Add the light to the scene
    scene.addEntity(lightEntity)
}

/**
 * Loads an HDR environment from assets and applies it to the scene.
 *
 * @param assets AssetManager to access asset files.
 * @param engine The Filament engine instance.
 * @param hdrFilePath Path to the HDR file in assets.
 * @param scene The scene where the environment will be applied.
 */
fun createEnvironment(
    assets: AssetManager, engine: Engine, hdrFilePath: String, scene: Scene
) {
    try {
        // Load the HDR file as a byte buffer
        assets.open(hdrFilePath).use { input ->
            val bytes = input.readBytes()
            val buffer = ByteBuffer.allocateDirect(bytes.size).apply {
                order(ByteOrder.nativeOrder())
                put(bytes)
                rewind()
            }

            // Convert HDR file into a texture
            val hdrTexture = HDRLoader.createTexture(engine, buffer)
            if (hdrTexture == null) {
                Log.e("HDRUtils", "Failed to load HDR texture from file: $hdrFilePath")
                return
            }

            // Convert HDR equirectangular texture to a cubemap
            val context = IBLPrefilterContext(engine)
            val equirectToCubemap = IBLPrefilterContext.EquirectangularToCubemap(context)
            val skyboxTexture = equirectToCubemap.run(hdrTexture)

            // Free memory of the original HDR texture
            engine.destroyTexture(hdrTexture)

            // Generate specular reflections
            val specularFilter = IBLPrefilterContext.SpecularFilter(context)
            val reflections = specularFilter.run(skyboxTexture)

            // Create Indirect Light (IBL - Image-Based Lighting)
            val ibl = IndirectLight.Builder().reflections(reflections)
                .intensity(30_000.0f) // Adjust brightness if needed
                .build(engine)

            // Create a Skybox from the cubemap
            val skybox = Skybox.Builder().environment(skyboxTexture).build(engine)

            // Apply the lighting and skybox to the scene
            scene.skybox = skybox
            scene.indirectLight = ibl

            // Clean up resources
            specularFilter.destroy()
            equirectToCubemap.destroy()
            context.destroy()

            Log.d("HDRUtils", "HDR environment loaded successfully from file: $hdrFilePath")
        }
    } catch (e: Exception) {
        Log.e("HDRUtils", "Error loading HDR file: $hdrFilePath", e)
    }
}