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
package com.shaikhmohammadtalha.anatomyinsight.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for performing queries on the `models` table.
 *
 * Provides access to all anatomical models and lookup functionality
 * based on model name or ID.
 */
@Dao
interface ModelDao {

    /**
     * Returns a flow of all anatomical models sorted by their ID.
     * Useful for listing models in the UI.
     */
    @Query("SELECT * FROM models ORDER BY id ASC")
    fun getAllModels(): Flow<List<AnatomyModelEntity>>

    /**
     * Fetches a single model by its common name.
     *
     * @param modelName The name of the model.
     * @return A flow emitting the matched AnatomyModelEntity.
     */
    @Query("SELECT * FROM models WHERE name = :modelName LIMIT 1")
    fun getModelByName(modelName: String): Flow<AnatomyModelEntity>

    /**
     * Fetches a single model by its unique ID.
     *
     * @param modelId The ID of the model.
     * @return A flow emitting the model or null if not found.
     */
    @Query("SELECT * FROM models WHERE id = :modelId")
    fun getModelById(modelId: Int): Flow<AnatomyModelEntity?>
}
