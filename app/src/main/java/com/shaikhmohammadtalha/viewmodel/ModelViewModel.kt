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
package com.shaikhmohammadtalha.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.shaikhmohammadtalha.anatomyinsight.AnatomyModel
import com.shaikhmohammadtalha.data.AnatomyDatabase
import com.shaikhmohammadtalha.data.AnatomyModelEntity
import com.shaikhmohammadtalha.data.SubpartEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

open class ModelViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AnatomyDatabase.getDatabase(application)
    private val modelDao = db.modelDao()
    private val subpartDao = db.subpartDao() // ✅ Add SubpartDao

    val allModels: Flow<List<AnatomyModelEntity>> = modelDao.getAllModels()

    // ✅ Fetch subparts dynamically from the database
    fun fetchSubParts(modelName: String): Flow<List<AnatomyModel>> {
        return subpartDao.getSubpartsForModel(modelName).map { subparts ->
            subparts.map { subpart ->
                AnatomyModel(
                    name = subpart.name,
                    filePath = "models/${subpart.modelName}/${subpart.name}.glb" // ✅ Generate dynamically
                )
            }
        }
    }

    open fun getSubpartByName(subpartName: String): Flow<SubpartEntity?> {
        return subpartDao.getSubpartByName(subpartName).map { it }
    }

    fun getModelByName(modelName: String): Flow<AnatomyModelEntity?> {
        return modelDao.getModelByName(modelName).map { it }
    }

}

