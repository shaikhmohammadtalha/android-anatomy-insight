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

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) for querying subparts of anatomical models.
 * Provides methods to fetch subparts by model name, ID, or perform full-text search.
 */
@Dao
interface SubpartDao {

    /**
     * Retrieves all subparts associated with a given anatomical model.
     *
     * @param modelName Name of the parent model.
     * @return A Flow emitting a list of SubpartResult entries.
     */
    @Query("""
        SELECT subparts.name, 
               subparts.scientific_name, 
               subparts.description, 
               models.name AS model_name 
        FROM subparts 
        JOIN models ON subparts.model_id = models.id 
        WHERE models.name = :modelName
    """)
    fun getSubpartsForModel(modelName: String): Flow<List<SubpartResult>>

    /**
     * Retrieves a subpart entity by its unique name.
     *
     * @param subpartName Name of the subpart.
     * @return A Flow emitting the corresponding SubpartEntity, or null if not found.
     */
    @Query("SELECT * FROM subparts WHERE name = :subpartName LIMIT 1")
    fun getSubpartByName(subpartName: String): Flow<SubpartEntity?>

    /**
     * Searches subparts by scientific name or the first line of their description.
     * Skips the first 6 entries to avoid displaying commonly used or default entries.
     *
     * @param query Search string.
     * @return A Flow emitting up to 20 matching SubpartEntity entries.
     */
    @Query("""
    SELECT * FROM subparts 
    WHERE id NOT IN (
        SELECT id FROM subparts ORDER BY id LIMIT 6
    ) 
    AND (
        scientific_name LIKE '%' || :query || '%' 
        OR substr(description, 1, instr(description || CHAR(10), CHAR(10)) - 1) LIKE '%' || :query || '%' 
    )    
    LIMIT :limit
""")
    fun searchSubpartsByScientificName(query: String, limit: Int): Flow<List<SubpartEntity>>

    /**
     * Fetches a subpart by its database ID.
     *
     * @param subpartId ID of the subpart.
     * @return A Flow emitting the SubpartEntity, or null if not found.
     */
    @Query("SELECT * FROM subparts WHERE id = :subpartId")
    fun getSubpartById(subpartId: Int): Flow<SubpartEntity?>
}

/**
 * Represents a simplified projection of subpart data used in model-subpart join queries.
 */
data class SubpartResult(
    val name: String,

    @ColumnInfo(name = "scientific_name")
    val scientificName: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "model_name")
    val modelName: String
)
