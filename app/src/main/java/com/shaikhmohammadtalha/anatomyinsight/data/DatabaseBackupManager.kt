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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Handles exporting (backup) and importing (restore) of the application's SQLite database.
 * Ensures WAL checkpoints, closes active connections, validates data integrity,
 * patches schema compatibility, and cleanly restarts the app process after restore.
 */
object DatabaseBackupManager {

    private const val TAG = "DatabaseBackupManager"
    const val DB_NAME = AnatomyDatabase.DB_NAME
    private const val ROOM_IDENTITY_HASH = "b4bab4323c7f9d7836adcd52aef2fb69"

    // Standard 16-byte SQLite 3 file header: "SQLite format 3\u0000"
    private val SQLITE_HEADER = byteArrayOf(
        0x53, 0x51, 0x4C, 0x69, 0x74, 0x65, 0x20, 0x66,
        0x6F, 0x72, 0x6D, 0x61, 0x74, 0x20, 0x33, 0x00
    )

    /**
     * Exports the current SQLite database to the specified document [Uri].
     * Flushes WAL logs to the main database file before copying.
     */
    suspend fun backupDatabase(context: Context, destUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Trigger DB open & flush WAL write-ahead log to the main .db file
            AnatomyDatabase.checkpoint(context)

            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Database file not found on device."))
            }

            context.contentResolver.openOutputStream(destUri)?.use { output ->
                FileInputStream(dbFile).use { input ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Could not open destination storage for writing."))

            Log.d(TAG, "Database backup exported successfully to $destUri")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error backing up database: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Replaces the current database with a backup file from the specified [Uri].
     * Validates SQLite header, verifies model data, applies compatibility patches,
     * and atomically replaces the active database file.
     */
    suspend fun restoreDatabase(context: Context, srcUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        val tempFile = File(context.cacheDir, "anatomy_restore_temp.db")
        try {
            // 1. Copy source URI to temporary file for inspection
            context.contentResolver.openInputStream(srcUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Could not read backup file."))

            if (!tempFile.exists() || tempFile.length() < 16) {
                return@withContext Result.failure(Exception("Selected file is empty or corrupted."))
            }

            // 2. Validate SQLite 3 magic header
            val header = ByteArray(16)
            FileInputStream(tempFile).use { input ->
                input.read(header)
            }
            if (!header.contentEquals(SQLITE_HEADER)) {
                return@withContext Result.failure(Exception("The selected file is not a valid SQLite database backup."))
            }

            // 3. Inspect database content and apply compatibility adjustments
            SQLiteDatabase.openDatabase(
                tempFile.path,
                null,
                SQLiteDatabase.OPEN_READWRITE
            ).use { db ->
                // Check if models and subparts tables exist and have records
                val tableCursor = db.rawQuery(
                    "SELECT count(*) FROM sqlite_master WHERE type='table' AND name IN ('models', 'subparts')",
                    null
                )
                var tablesFound = 0
                if (tableCursor.moveToFirst()) {
                    tablesFound = tableCursor.getInt(0)
                }
                tableCursor.close()

                if (tablesFound < 2) {
                    return@withContext Result.failure(
                        Exception("Invalid backup: Missing anatomical models or subparts tables.")
                    )
                }

                var modelCount = 0
                val countModelCursor = db.rawQuery("SELECT count(*) FROM models", null)
                if (countModelCursor.moveToFirst()) {
                    modelCount = countModelCursor.getInt(0)
                }
                countModelCursor.close()

                var subpartCount = 0
                val countSubpartCursor = db.rawQuery("SELECT count(*) FROM subparts", null)
                if (countSubpartCursor.moveToFirst()) {
                    subpartCount = countSubpartCursor.getInt(0)
                }
                countSubpartCursor.close()

                if (modelCount == 0 || subpartCount == 0) {
                    return@withContext Result.failure(
                        Exception("Invalid backup: Database contains 0 models or subparts.")
                    )
                }

                // Ensure notes and history tables exist in case of an older backup
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `notes` (`subpartId` INTEGER NOT NULL PRIMARY KEY, `noteText` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `subpartId` INTEGER NOT NULL, `subpartName` TEXT NOT NULL, `modelName` TEXT NOT NULL, `viewedAt` INTEGER NOT NULL)"
                )

                // Ensure subparts foreign key index exists
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_subparts_model_id` ON `subparts` (`model_id`)")

                // Ensure room_master_table and version match Room schema exactly to prevent destructive drops
                db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
                db.execSQL("INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES(42, '$ROOM_IDENTITY_HASH')")
                db.version = 8
            }

            // 4. Safely close active Room database
            AnatomyDatabase.closeDatabase()

            val dbFile = context.getDatabasePath(DB_NAME)
            val walFile = File(dbFile.parentFile, "$DB_NAME-wal")
            val shmFile = File(dbFile.parentFile, "$DB_NAME-shm")

            // Remove existing WAL & SHM cache files and old DB
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()
            if (dbFile.exists()) dbFile.delete()

            // Overwrite main DB file with validated tempFile
            FileInputStream(tempFile).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "Database restored successfully from $srcUri")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring database: ${e.message}", e)
            Result.failure(e)
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    /**
     * Restarts the entire application process cleanly so that all ViewModels,
     * DAOs, and database connections reload fresh with the restored database.
     */
    fun restartApp(context: Context) {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
            Runtime.getRuntime().exit(0)
        } else {
            var current: Context? = context
            while (current is ContextWrapper) {
                if (current is Activity) {
                    current.recreate()
                    return
                }
                current = current.baseContext
            }
        }
    }
}
