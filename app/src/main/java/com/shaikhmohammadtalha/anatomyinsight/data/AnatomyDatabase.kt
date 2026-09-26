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
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File

/**
 * Room database definition for anatomy-related entities.
 *
 * Version history:
 *   v6 – original release (AnatomyModelEntity, SubpartEntity)
 *   v7 – added NoteEntity for per-subpart user notes
 *   v8 – added HistoryEntity for recently-viewed subpart tracking
 */
@Database(
    entities = [
        AnatomyModelEntity::class,
        SubpartEntity::class,
        NoteEntity::class,
        HistoryEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AnatomyDatabase : RoomDatabase() {

    /** DAO for querying anatomical models */
    abstract fun modelDao(): ModelDao

    /** DAO for querying anatomical subparts */
    abstract fun subpartDao(): SubpartDao

    /** DAO for reading and writing user notes on subparts */
    abstract fun noteDao(): NoteDao

    /** DAO for recording and querying the viewing history */
    abstract fun historyDao(): HistoryDao

    companion object {
        const val DB_NAME = "anatomy_database_fixed.db"

        @Volatile
        private var INSTANCE: AnatomyDatabase? = null

        private val MIGRATION_6_8 = object : Migration(6, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `notes` (`subpartId` INTEGER NOT NULL PRIMARY KEY, `noteText` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `subpartId` INTEGER NOT NULL, `subpartName` TEXT NOT NULL, `modelName` TEXT NOT NULL, `viewedAt` INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `subpartId` INTEGER NOT NULL, `subpartName` TEXT NOT NULL, `modelName` TEXT NOT NULL, `viewedAt` INTEGER NOT NULL)"
                )
            }
        }

        /**
         * Ensures the database file on disk is valid. If it was corrupted or emptied
         * by a failed migration or interrupted restore, this deletes the damaged files
         * so Room's [createFromAsset] can safely re-seed from the packaged database.
         */
        private fun ensureValidDatabaseFile(context: Context) {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists() || dbFile.length() < 16) return

            var isValid = false
            try {
                android.database.sqlite.SQLiteDatabase.openDatabase(
                    dbFile.path,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                ).use { db ->
                    val cursor = db.rawQuery(
                        "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='models'",
                        null
                    )
                    var hasModelsTable = false
                    if (cursor.moveToFirst()) {
                        hasModelsTable = cursor.getInt(0) > 0
                    }
                    cursor.close()

                    if (hasModelsTable) {
                        val countCursor = db.rawQuery("SELECT count(*) FROM models", null)
                        if (countCursor.moveToFirst()) {
                            isValid = countCursor.getInt(0) > 0
                        }
                        countCursor.close()
                    }
                }
            } catch (e: Exception) {
                Log.w("AnatomyDatabase", "Existing database check failed: ${e.message}")
                isValid = false
            }

            if (!isValid) {
                Log.w("AnatomyDatabase", "Existing database file on device is invalid or empty. Resetting from bundled asset.")
                val walFile = File(dbFile.parentFile, "$DB_NAME-wal")
                val shmFile = File(dbFile.parentFile, "$DB_NAME-shm")
                if (dbFile.exists()) dbFile.delete()
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()
            }
        }

        /**
         * Returns the singleton database instance.
         * Uses a pre-packaged asset and falls back to destructive migration on schema changes.
         *
         * @param context Application context
         */
        fun getDatabase(context: Context): AnatomyDatabase {
            return INSTANCE ?: synchronized(this) {
                ensureValidDatabaseFile(context.applicationContext)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnatomyDatabase::class.java,
                    DB_NAME
                )
                    .createFromAsset(DB_NAME)
                    .addMigrations(MIGRATION_6_8, MIGRATION_7_8)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Flushes the SQLite write-ahead log (WAL) into the main database file.
         */
        fun checkpoint(context: Context) {
            synchronized(this) {
                try {
                    val db = getDatabase(context)
                    val cursor = db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")
                    cursor.moveToFirst()
                    cursor.close()
                } catch (e: Exception) {
                    Log.e("AnatomyDatabase", "WAL checkpoint failed: ${e.message}", e)
                }
            }
        }

        /**
         * Closes the active Room database and clears the singleton instance.
         */
        fun closeDatabase() {
            synchronized(this) {
                INSTANCE?.let { db ->
                    if (db.isOpen) {
                        try {
                            val cursor = db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")
                            cursor.moveToFirst()
                            cursor.close()
                        } catch (_: Exception) {}
                        try {
                            db.close()
                        } catch (e: Exception) {
                            Log.e("AnatomyDatabase", "Database close failed: ${e.message}")
                        }
                    }
                }
                INSTANCE = null
            }
        }
    }
}
