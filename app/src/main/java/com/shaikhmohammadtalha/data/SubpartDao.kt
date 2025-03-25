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

@Dao
interface SubpartDao {
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

    @Query("SELECT * FROM subparts WHERE name = :subpartName LIMIT 1")
    fun getSubpartByName(subpartName: String): Flow<SubpartEntity?>

}


data class SubpartResult(
    val name: String,

    @ColumnInfo(name = "scientific_name") // ✅ Matches DB column name
    val scientificName: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "model_name")
    val model_name: String
)

