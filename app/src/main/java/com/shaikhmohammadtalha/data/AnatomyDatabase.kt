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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AnatomyModelEntity::class, SubpartEntity::class], version = 6, exportSchema = false)
abstract class AnatomyDatabase : RoomDatabase() {
    abstract fun modelDao(): ModelDao
    abstract fun subpartDao(): SubpartDao

    companion object {
        @Volatile
        private var INSTANCE: AnatomyDatabase? = null

        fun getDatabase(context: Context): AnatomyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnatomyDatabase::class.java,
                    "anatomy_database_fixed.db"
                )
                    .fallbackToDestructiveMigration() // ✅ Deletes old DB if schema mismatches
                    .createFromAsset("anatomy_database_fixed.db") // ✅ Loads pre-packaged database directly
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}