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
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * DAO for reading and writing user notes on anatomical subparts.
 */
@Dao
interface NoteDao {

    /**
     * Returns the note for a given subpart ID, or null if none exists.
     *
     * @param subpartId The ID of the subpart
     */
    @Query("SELECT * FROM notes WHERE subpartId = :subpartId LIMIT 1")
    fun getNoteForSubpart(subpartId: Int): Flow<NoteEntity?>

    /**
     * Inserts or updates a note. If a note for this subpart already exists it is replaced.
     *
     * @param note The note to upsert
     */
    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    /**
     * Returns all notes ordered by most recently updated.
     */
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    /**
     * Deletes the note for a given subpart ID.
     *
     * @param subpartId The ID of the subpart whose note should be deleted
     */
    @Query("DELETE FROM notes WHERE subpartId = :subpartId")
    suspend fun deleteNote(subpartId: Int)
}
