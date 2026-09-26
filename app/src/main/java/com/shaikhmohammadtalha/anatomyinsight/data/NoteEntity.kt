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

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a user's personal note attached to an anatomical subpart.
 * Notes use a 1:1 relationship with subparts (keyed by subpartId).
 *
 * @param subpartId Foreign key referencing subparts.id (also primary key)
 * @param noteText The note text entered by the user
 * @param updatedAt Epoch milliseconds of the last update
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val subpartId: Int,
    val noteText: String,
    val updatedAt: Long = System.currentTimeMillis()
)
