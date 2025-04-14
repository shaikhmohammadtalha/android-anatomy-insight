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
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Defines the available application theme modes.
 */
enum class AppTheme {
    AUTO,    // Follows system setting or decides dynamically based on device capabilities
    LIGHT,   // Forces light mode
    DARK,    // Forces dark mode
    DYNAMIC  // Uses dynamic theming (Material You) on supported Android versions
}

/**
 * Manages the persistence and retrieval of the selected [AppTheme] using Jetpack DataStore.
 */
object ThemeStore {

    // Scoped DataStore instance for storing theme preference
    private val Context.dataStore by preferencesDataStore("theme_settings")

    // Preference key for the saved theme
    private val THEME = stringPreferencesKey("app_theme")

    /**
     * Saves the selected [AppTheme] into persistent storage.
     *
     * @param context Android context used to access the DataStore.
     * @param theme The selected application theme to be stored.
     */
    suspend fun saveTheme(context: Context, theme: AppTheme) {
        context.dataStore.edit { it[THEME] = theme.name }
    }

    /**
     * Provides a reactive [Flow] that emits the current [AppTheme].
     * Defaults to [AppTheme.AUTO] if no theme has been previously saved.
     */
    val themeFlow: (Context) -> Flow<AppTheme> = { context ->
        context.dataStore.data
            .map { it[THEME]?.let(AppTheme::valueOf) ?: AppTheme.AUTO }
    }
}
