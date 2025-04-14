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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database definition for anatomy-related entities.
 * Includes model and subpart tables and provides DAOs for data access.
 */
@Database(
    entities = [AnatomyModelEntity::class, SubpartEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AnatomyDatabase : RoomDatabase() {

    // Data Access Object for anatomy models
    abstract fun modelDao(): ModelDao

    // Data Access Object for model subparts
    abstract fun subpartDao(): SubpartDao

    companion object {
        @Volatile
        private var INSTANCE: AnatomyDatabase? = null

        /**
         * Provides a singleton instance of the [AnatomyDatabase].
         * Uses a pre-packaged asset and handles migrations destructively if needed.
         *
         * @param context Application context for database creation.
         * @return The initialized Room database instance.
         */
        fun getDatabase(context: Context): AnatomyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnatomyDatabase::class.java,
                    "anatomy_database_fixed.db"
                )
                    .fallbackToDestructiveMigration() // Resets DB if schema changes (non-migrated)
                    .createFromAsset("anatomy_database_fixed.db") // Uses a pre-built database from assets
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
