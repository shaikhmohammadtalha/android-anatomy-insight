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
package com.shaikhmohammadtalha.anatomyinsight.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shaikhmohammadtalha.anatomyinsight.ui.model.AnatomyModel
import com.shaikhmohammadtalha.anatomyinsight.data.AnatomyDatabase
import com.shaikhmohammadtalha.anatomyinsight.data.AnatomyModelEntity
import com.shaikhmohammadtalha.anatomyinsight.data.HistoryEntity
import com.shaikhmohammadtalha.anatomyinsight.data.NoteEntity
import com.shaikhmohammadtalha.anatomyinsight.data.SubpartEntity
import com.shaikhmohammadtalha.anatomyinsight.datastore.GraphicsSettingsStore.SEARCH_LIMIT
import com.shaikhmohammadtalha.anatomyinsight.datastore.GraphicsSettingsStore.dataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for accessing and managing anatomy models, subparts, user notes, and viewing history.
 * All database access is abstracted here and exposed as reactive Kotlin Flows.
 */
open class ModelViewModel(application: Application) : AndroidViewModel(application) {

    private val db get() = AnatomyDatabase.getDatabase(getApplication())
    private val modelDao get() = db.modelDao()
    private val subpartDao get() = db.subpartDao()
    private val noteDao get() = db.noteDao()
    private val historyDao get() = db.historyDao()

    // ─── Models ────────────────────────────────────────────────────────────────

    /** All anatomy models as a reactive stream, ordered by ID. */
    val allModels: Flow<List<AnatomyModelEntity>>
        get() = modelDao.getAllModels()

    /**
     * Fetches subparts for a model and converts them into UI-ready [AnatomyModel] objects.
     *
     * @param modelName The anatomical system name
     */
    fun fetchSubParts(modelName: String): Flow<List<AnatomyModel>> {
        return subpartDao.getSubpartsForModel(modelName).map { subparts ->
            subparts.map { subpart ->
                AnatomyModel(
                    name = subpart.name,
                    filePath = "models/$modelName/${subpart.name}.glb"
                )
            }
        }
    }

    /**
     * Searches subparts by scientific name or description first line.
     *
     * @param query Partial or full scientific name
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun searchSubpartsByScientificName(query: String): Flow<List<SubpartEntity>> {
        return getSearchLimit().flatMapLatest { limit ->
            subpartDao.searchSubpartsByScientificName(query, limit)
        }
    }

    /**
     * Fetches a subpart entity by its common name.
     *
     * @param subpartName Exact common name of the subpart
     */
    open fun getSubpartByName(subpartName: String): Flow<SubpartEntity?> {
        return subpartDao.getSubpartByName(subpartName).map { it }
    }

    /**
     * Fetches a model entity by its name.
     *
     * @param modelName The anatomical system name
     */
    fun getModelByName(modelName: String): Flow<AnatomyModelEntity?> {
        return modelDao.getModelByName(modelName).map { it }
    }

    /**
     * Fetches a subpart entity by its unique database ID.
     *
     * @param subpartId The subpart's primary key
     */
    fun getSubpartById(subpartId: Int): Flow<SubpartEntity?> {
        return subpartDao.getSubpartById(subpartId).map { it }
    }

    /**
     * Fetches a model entity by its unique database ID.
     *
     * @param modelId The model's primary key
     */
    fun getModelById(modelId: Int): Flow<AnatomyModelEntity?> {
        return modelDao.getModelById(modelId).map { it }
    }

    /**
     * Retrieves the user-configured search result limit from DataStore.
     * Defaults to 20 if not set.
     */
    fun getSearchLimit(): Flow<Int> {
        val context = getApplication<Application>().applicationContext
        return context.dataStore.data.map { prefs -> prefs[SEARCH_LIMIT] ?: 20 }
    }

    // ─── Notes ─────────────────────────────────────────────────────────────────

    /**
     * Returns the note for a given subpart, or null if none exists.
     *
     * @param subpartId The ID of the subpart
     */
    fun getNoteForSubpart(subpartId: Int): Flow<NoteEntity?> =
        noteDao.getNoteForSubpart(subpartId)

    /**
     * Inserts or updates the note for a subpart.
     *
     * @param subpartId The ID of the subpart
     * @param text The note text to persist
     */
    fun upsertNote(subpartId: Int, text: String) {
        viewModelScope.launch {
            noteDao.upsertNote(NoteEntity(subpartId = subpartId, noteText = text))
        }
    }

    /**
     * Returns all user notes ordered by most recently updated.
     */
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    /**
     * Deletes the note for a given subpart.
     *
     * @param subpartId The ID of the subpart whose note should be deleted
     */
    fun deleteNote(subpartId: Int) {
        viewModelScope.launch { noteDao.deleteNote(subpartId) }
    }

    // ─── History ────────────────────────────────────────────────────────────────

    /**
     * Records that the user viewed a subpart and prunes old entries.
     *
     * @param subpartId The ID of the viewed subpart
     * @param subpartName Display name of the subpart
     * @param modelName Name of the parent anatomical system
     */
    fun recordHistory(subpartId: Int, subpartName: String, modelName: String) {
        viewModelScope.launch {
            historyDao.recordView(
                HistoryEntity(
                    subpartId = subpartId,
                    subpartName = subpartName,
                    modelName = modelName
                )
            )
            historyDao.pruneOldEntries()
        }
    }

    /**
     * Returns up to 100 most recently viewed subparts.
     */
    fun getRecentHistory(): Flow<List<HistoryEntity>> = historyDao.getRecentHistory()

    /**
     * Returns up to 3 most recently viewed subparts from a specific anatomical system.
     *
     * @param modelName The anatomical system name
     */
    fun getRecentHistoryForModel(modelName: String): Flow<List<HistoryEntity>> =
        historyDao.getRecentForModel(modelName)

    /**
     * Deletes a single viewing history entry.
     *
     * @param id The primary key ID of the history entry to remove
     */
    fun deleteHistoryEntry(id: Int) {
        viewModelScope.launch { historyDao.deleteEntry(id) }
    }

    /**
     * Clears all viewing history.
     */
    fun clearHistory() {
        viewModelScope.launch { historyDao.clearHistory() }
    }
}
