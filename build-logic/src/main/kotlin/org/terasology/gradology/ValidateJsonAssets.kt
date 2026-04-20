// Copyright 2024 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.json.JSONException
import org.json.JSONObject
import java.io.File

/**
 * Gradle task that validates JSON assets (prefabs, blocks, ui, etc.) at build time.
 *
 * Iterates over all configured JSON asset files and attempts to parse each one.
 * If any file contains malformed JSON, the build fails with a descriptive error message.
 */
abstract class ValidateJsonAssets : DefaultTask() {

    init {
        group = "Verification"
        description = "Validates that all JSON assets (prefabs, blocks, ui, etc.) are well-formed."
    }

    @get:InputFiles
    val jsonAssets: MutableList<ConfigurableFileTree> = mutableListOf()

    /**
     * Add a file tree of JSON assets to validate.
     */
    fun source(fileTree: ConfigurableFileTree) {
        jsonAssets.add(fileTree)
    }

    @TaskAction
    fun validate() {
        val errors = mutableListOf<String>()

        for (fileTree in jsonAssets) {
            for (file in fileTree) {
                validateFile(file)?.let { errors.add(it) }
            }
        }

        if (errors.isNotEmpty()) {
            val message = buildString {
                appendLine("Found ${errors.size} invalid JSON asset(s):")
                errors.forEach { appendLine("  - $it") }
            }
            throw GradleException(message)
        }

        logger.lifecycle("All JSON assets are valid.")
    }

    private fun validateFile(file: File): String? {
        return try {
            JSONObject(file.readText())
            null  // valid
        } catch (e: JSONException) {
            "${file.path}: ${e.message}"
        }
    }
}
