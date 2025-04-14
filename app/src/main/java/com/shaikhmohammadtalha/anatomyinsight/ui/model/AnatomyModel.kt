package com.shaikhmohammadtalha.anatomyinsight.ui.model

/**
 * UI-friendly representation of an anatomical model or subpart.
 * This is used throughout the UI layer to represent loaded models.
 *
 * @param name Display name of the model or subpart
 * @param filePath Relative path to the associated .glb 3D model file
 */
data class AnatomyModel(
    val name: String,
    val filePath: String
)
