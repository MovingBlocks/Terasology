// Copyright 2024 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.json.JSONException
import org.json.JSONObject
import java.io.File

/**
 * Gradle task that validates JSON assets (prefabs, blocks, ui, etc.) at build time.
 *
 * Iterates over all configured JSON asset files and attempts to parse each one.
 * If any file contains malformed JSON, the build fails with a descriptive error message.
 *
 * Example usage in a build script:
 * ```kotlin
 * tasks.register<ValidateJsonAssets>("validateJsonAssets") {
 *     source(fileTree("assets") { include("**&#47;*.prefab", "**&#47;*.json") })
 * }
 * ```
 */
abstract class ValidateJsonAssets : DefaultTask() {

    init {
        group = "Verification"
        description = "Validates that all JSON assets (prefabs, blocks, ui, etc.) are well-formed."
    }

    /**
     * The set of JSON asset files to validate.
     * Use [source] to add file trees.
     */
    @get:InputFiles
    @get:SkipWhenEmpty
    val jsonAssets: ConfigurableFileCollection = project.files()

    /**
     * Add files or file trees of JSON assets to validate.
     */
    fun source(vararg paths: Any) {
        jsonAssets.from(*paths)
    }

    @TaskAction
    fun validate() {
        val errors = mutableListOf<String>()

        for (file in jsonAssets) {
            validateFile(file)?.let { errors.add(it) }
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
