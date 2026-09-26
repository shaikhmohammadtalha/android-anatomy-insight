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
 * Room entity recording that a user viewed a particular anatomical subpart.
 * The history list is capped to the 100 most recent entries via [HistoryDao.pruneOldEntries].
 *
 * @param id Auto-generated primary key
 * @param subpartId Database ID of the viewed subpart
 * @param subpartName Name of the viewed subpart
 * @param modelName Name of the parent model system
 * @param viewedAt Epoch millisecond timestamp of when the subpart was viewed
 */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subpartId: Int,
    val subpartName: String,
    val modelName: String,
    val viewedAt: Long = System.currentTimeMillis()
)
