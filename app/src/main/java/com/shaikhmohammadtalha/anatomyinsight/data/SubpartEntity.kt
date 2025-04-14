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
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Room Entity representing a subpart of an anatomical model.
 *
 * Each subpart is linked to a parent model via a foreign key relationship.
 * The `subparts` table stores metadata such as the subpart's name, scientific name, and description.
 */
@Entity(
    tableName = "subparts",
    foreignKeys = [ForeignKey(
        entity = AnatomyModelEntity::class,
        parentColumns = ["id"],
        childColumns = ["model_id"],
        onDelete = ForeignKey.CASCADE // Ensures subparts are deleted if their parent model is deleted
    )]
)
data class SubpartEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,  // Auto-generated primary key for the subpart

    @ColumnInfo(name = "model_id")
    val modelId: Int,  // Foreign key linking to the parent model

    @ColumnInfo(name = "name")
    val name: String,  // Common name of the subpart

    @ColumnInfo(name = "scientific_name")
    val scientificName: String = "Unknown",  // Scientific name with a default fallback

    @ColumnInfo(name = "description")
    val description: String = "No description available"  // Text description with default content
)
