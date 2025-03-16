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

fun createLights(engine: Engine, scene: Scene) {
    val entityManager = EntityManager.get()
    val lightEntity = entityManager.create()

    // Create a directional light
    LightManager.Builder(LightManager.Type.DIRECTIONAL)
        .color(1.0f, 1.0f, 1.0f) // White light
        .intensity(50_000.0f) // Adjust intensity as needed
        .direction(0.0f, -1.0f, 0.0f) // Direction pointing downwards
        .castShadows(true)
        .build(engine, lightEntity)

    // Add the light to the scene
    scene.addEntity(lightEntity)
}

fun createEnvironment(
    assets: AssetManager,
    engine: Engine,
    hdrFilePath: String,
    scene: Scene
) {
    try {
        // Load the HDR file from assets
        assets.open(hdrFilePath).use { input ->
            val bytes = input.readBytes()
            val buffer = ByteBuffer.allocateDirect(bytes.size).apply {
                order(ByteOrder.nativeOrder())
                put(bytes)
                rewind()
            }

            // Load HDR texture and create equirectangular texture
            val hdrTexture = HDRLoader.createTexture(engine, buffer)
            if (hdrTexture == null) {
                Log.e("HDRUtils", "Failed to load HDR texture from file: $hdrFilePath")
                return
            }

            // Convert equirectangular texture to cubemap
            val context = IBLPrefilterContext(engine)
            val equirectToCubemap = IBLPrefilterContext.EquirectangularToCubemap(context)
            val skyboxTexture = equirectToCubemap.run(hdrTexture)
            engine.destroyTexture(hdrTexture) // Destroy original texture to free memory

            // Create the specular filter for reflections
            val specularFilter = IBLPrefilterContext.SpecularFilter(context)
            val reflections = specularFilter.run(skyboxTexture)

            // Create the indirect light (image-based lighting)
            val ibl = IndirectLight.Builder()
                .reflections(reflections)
                .intensity(30_000.0f) // Adjust intensity as per your scene's requirement
                .build(engine)

            // Create the skybox
            val skybox = Skybox.Builder()
                .environment(skyboxTexture)
                .build(engine)

            // Apply indirect light and skybox to the scene
            scene.skybox = skybox
            scene.indirectLight = ibl

            // Cleanup
            specularFilter.destroy()
            equirectToCubemap.destroy()
            context.destroy()

            Log.d("HDRUtils", "HDR environment loaded successfully from file: $hdrFilePath")
        }
    } catch (e: Exception) {
        Log.e("HDRUtils", "Error loading HDR file: $hdrFilePath", e)
    }
}
