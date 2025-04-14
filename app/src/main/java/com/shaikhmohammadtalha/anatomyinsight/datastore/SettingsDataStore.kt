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
package com.shaikhmohammadtalha.anatomyinsight.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Stores and retrieves user-defined graphics settings using Jetpack DataStore.
 * These settings influence real-time rendering quality in the 3D viewer.
 */
object GraphicsSettingsStore {

    // Backing DataStore instance scoped to context, using "graphics_settings" as the preferences file
    val Context.dataStore by preferencesDataStore(name = "graphics_settings")

    // Preference keys for various graphics options
    private val QUALITY = stringPreferencesKey("render_quality")
    private val MSAA = booleanPreferencesKey("msaa_enabled")
    private val AO = booleanPreferencesKey("ao_enabled")
    private val BLOOM = booleanPreferencesKey("bloom_enabled")

    val SEARCH_LIMIT = intPreferencesKey("search_limit")

    /**
     * Saves the selected graphics settings to persistent storage.
     *
     * @param context Android context for accessing the DataStore.
     * @param quality Render quality (e.g., LOW, MEDIUM, HIGH, ULTRA).
     * @param msaa Enable/disable multi-sample anti-aliasing.
     * @param ao Enable/disable ambient occlusion.
     * @param bloom Enable/disable bloom effect.
     */
    suspend fun saveSettings(
        context: Context, quality: String, msaa: Boolean, ao: Boolean, bloom: Boolean, searchLimit: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[QUALITY] = quality
            prefs[MSAA] = msaa
            prefs[AO] = ao
            prefs[BLOOM] = bloom
            prefs[SEARCH_LIMIT] = searchLimit
        }
    }

    /**
     * Provides a reactive Flow of current graphics settings.
     * Emits default values if settings are not yet stored.
     */
    val settingsFlow: (Context) -> Flow<GraphicsSettings> = { context ->
        context.dataStore.data
            .catch { emit(emptyPreferences()) } // Handle I/O exceptions gracefully
            .map { prefs ->
                GraphicsSettings(
                    quality = prefs[QUALITY] ?: "LOW",
                    msaa = prefs[MSAA] ?: true,
                    ao = prefs[AO] ?: true,
                    bloom = prefs[BLOOM] ?: true,
                    searchLimit = prefs[SEARCH_LIMIT] ?: 20
                )
            }
    }
}

/**
 * Represents a set of configurable graphics options for rendering.
 *
 * @param quality Render quality level.
 * @param msaa Whether MSAA is enabled.
 * @param ao Whether ambient occlusion is enabled.
 * @param bloom Whether bloom is enabled.
 */
data class GraphicsSettings(
    val quality: String, val msaa: Boolean, val ao: Boolean, val bloom: Boolean, val searchLimit: Int
)

