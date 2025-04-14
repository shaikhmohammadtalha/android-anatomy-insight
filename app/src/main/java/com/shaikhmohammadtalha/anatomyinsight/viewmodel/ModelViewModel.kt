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
import com.shaikhmohammadtalha.anatomyinsight.ui.model.AnatomyModel
import com.shaikhmohammadtalha.anatomyinsight.data.AnatomyDatabase
import com.shaikhmohammadtalha.anatomyinsight.data.AnatomyModelEntity
import com.shaikhmohammadtalha.anatomyinsight.data.SubpartEntity
import com.shaikhmohammadtalha.anatomyinsight.datastore.GraphicsSettingsStore.SEARCH_LIMIT
import com.shaikhmohammadtalha.anatomyinsight.datastore.GraphicsSettingsStore.dataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * ViewModel for accessing and managing anatomy models and subparts from the local database.
 * This layer abstracts database access logic and provides data as reactive streams using Kotlin Flow.
 */
open class ModelViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize database and DAOs
    private val db = AnatomyDatabase.getDatabase(application)
    private val modelDao = db.modelDao()
    private val subpartDao = db.subpartDao()

    // Expose all anatomy models as a reactive stream
    val allModels: Flow<List<AnatomyModelEntity>> = modelDao.getAllModels()

    /**
     * Fetch subparts associated with a given model name.
     * Each subpart is transformed into an [AnatomyModel] with a dynamically constructed file path.
     *
     * @param modelName The name of the model to fetch subparts for.
     * @return A Flow emitting a list of [AnatomyModel] objects.
     */
    fun fetchSubParts(modelName: String): Flow<List<AnatomyModel>> {
        return subpartDao.getSubpartsForModel(modelName).map { subparts ->
            subparts.map { subpart ->
                AnatomyModel(
                    name = subpart.name,
                    filePath = "models/${subpart.modelName}/${subpart.name}.glb"
                )
            }
        }
    }

    /**
     * Search for subparts using a scientific name query string.
     *
     * @param query Partial or full scientific name.
     * @return A Flow emitting matching subparts.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun searchSubpartsByScientificName(query: String): Flow<List<SubpartEntity>> {
        return getSearchLimit().flatMapLatest { limit ->
            subpartDao.searchSubpartsByScientificName(query, limit)
        }
    }


    /**
     * Retrieve a subpart entity by its name.
     *
     * @param subpartName The exact name of the subpart.
     * @return A Flow emitting the matching [SubpartEntity], if found.
     */
    open fun getSubpartByName(subpartName: String): Flow<SubpartEntity?> {
        return subpartDao.getSubpartByName(subpartName).map { it }
    }

    /**
     * Retrieve a model entity by its name.
     *
     * @param modelName The name of the model.
     * @return A Flow emitting the matching [AnatomyModelEntity], if found.
     */
    fun getModelByName(modelName: String): Flow<AnatomyModelEntity?> {
        return modelDao.getModelByName(modelName).map { it }
    }

    /**
     * Retrieve a subpart entity by its unique ID.
     *
     * @param subpartId The ID of the subpart.
     * @return A Flow emitting the matching [SubpartEntity], if found.
     */
    fun getSubpartById(subpartId: Int): Flow<SubpartEntity?> {
        return subpartDao.getSubpartById(subpartId).map { it }
    }

    /**
     * Retrieve a model entity by its unique ID.
     *
     * @param modelId The ID of the model.
     * @return A Flow emitting the matching [AnatomyModelEntity], if found.
     */
    fun getModelById(modelId: Int): Flow<AnatomyModelEntity?> {
        return modelDao.getModelById(modelId).map { it }
    }

    /**
     * Retrieves the maximum number of search results to return from settings.
     *
     * @return A Flow emitting the user-defined limit, or 20 if not set.
     */
    fun getSearchLimit(): Flow<Int> {
        val context = getApplication<Application>().applicationContext
        return context.dataStore.data
            .map { prefs -> prefs[SEARCH_LIMIT] ?: 20 }
    }
}
