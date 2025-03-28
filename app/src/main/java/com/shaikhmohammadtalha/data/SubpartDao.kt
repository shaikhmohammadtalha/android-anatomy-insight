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
package com.shaikhmohammadtalha.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) for querying subparts of anatomical models.
 */
@Dao
interface SubpartDao {

    /**
     * Retrieves all subparts associated with a specific model.
     *
     * @param modelName Name of the model whose subparts are needed.
     * @return A Flow of a list of `SubpartResult`, containing subpart details.
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
     * Retrieves a single subpart by its name.
     *
     * @param subpartName The name of the subpart to fetch.
     * @return A Flow emitting a single `SubpartEntity`, or null if not found.
     */
    @Query("SELECT * FROM subparts WHERE name = :subpartName LIMIT 1")
    fun getSubpartByName(subpartName: String): Flow<SubpartEntity?>

    @Query("""
    SELECT * FROM subparts 
    WHERE scientific_name LIKE '%' || :query || '%' 
    OR substr(description, 1, instr(description || CHAR(10), CHAR(10)) - 1) LIKE '%' || :query || '%' 
    LIMIT 20
""")
    fun searchSubpartsByScientificName(query: String): Flow<List<SubpartEntity>>

    @Query("SELECT * FROM subparts WHERE id = :subpartId")
    fun getSubpartById(subpartId: Int): Flow<SubpartEntity?>

}

/**
 * Data class representing the result of a subpart query.
 */
data class SubpartResult(
    val name: String,

    @ColumnInfo(name = "scientific_name")
    val scientificName: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "model_name")
    val modelName: String // Renamed for consistency
)