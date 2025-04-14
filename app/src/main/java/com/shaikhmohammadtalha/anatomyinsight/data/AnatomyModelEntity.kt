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
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing an anatomical model stored in the database.
 *
 * This class defines the structure of the `models` table which holds metadata
 * about each anatomical model including its name, scientific name, file path, and description.
 */
@Entity(tableName = "models")
data class AnatomyModelEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,  // Auto-generated primary key

    @ColumnInfo(name = "name")
    val name: String,  // Common name of the model

    @ColumnInfo(name = "scientific_name")
    val scientificName: String,  // Scientific name of the anatomical model

    @ColumnInfo(name = "file_path")
    val filePath: String,  // Path to the corresponding 3D model file

    @ColumnInfo(name = "description")
    val description: String  // Informational text or description of the model
)
