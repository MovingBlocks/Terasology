// Copyright 2024 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * The outcome of inspecting a single JSON asset.
 *
 * @property error why the file could not be parsed at all, or null if it parsed cleanly
 * @property warnings problems that do not stop the engine loading the file, but are still defects
 */
data class AssetInspection(val error: String?, val warnings: List<String>)

/**
 * Parses Terasology JSON assets the same way the engine does.
 *
 * Parsing deliberately mirrors the engine's own asset loaders rather than strict RFC 8259:
 * `UIFormat` and `UISkinFormat` call `JsonReader.setLenient(true)` outright, and the block and
 * prefab formats go through `Gson.fromJson`, which is lenient by default. Terasology's asset
 * format therefore permits slash-star licence headers and double-slash inline notes, and a large
 * share of shipped assets use them - `CoreAssets` alone has dozens. A strict parser here would
 * reject content the engine loads happily.
 *
 * Kept free of Gradle types so it can be tested directly, without standing up a nested build.
 */
object JsonAssetInspector {

    fun inspect(file: File): AssetInspection {
        val warnings = mutableListOf<String>()
        return try {
            file.bufferedReader().use { source ->
                val reader = JsonReader(source)
                reader.isLenient = true

                if (reader.peek() == JsonToken.END_DOCUMENT) {
                    return AssetInspection("${file.path}: file is empty", warnings)
                }

                walk(reader, file, "", warnings)

                if (reader.peek() != JsonToken.END_DOCUMENT) {
                    return AssetInspection(
                        "${file.path}: unexpected trailing content after the root value",
                        warnings
                    )
                }
            }
            AssetInspection(null, warnings)
        } catch (e: Exception) {
            AssetInspection("${file.path}: ${e.message ?: e.javaClass.simpleName}", warnings)
        }
    }

    /**
     * Walk the whole token stream. Reading every token is what proves the document parses;
     * tracking the names seen per object is what surfaces duplicate keys.
     *
     * Duplicate keys are warnings rather than errors: Gson silently keeps the last occurrence, so
     * a duplicate never breaks loading - but it does mean an earlier value is being discarded
     * without anyone noticing, which is nearly always a mistake.
     */
    private fun walk(reader: JsonReader, file: File, path: String, warnings: MutableList<String>) {
        when (reader.peek()) {
            JsonToken.BEGIN_OBJECT -> {
                reader.beginObject()
                val seen = mutableSetOf<String>()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    val childPath = if (path.isEmpty()) name else "$path.$name"
                    if (!seen.add(name)) {
                        warnings.add(
                            "${file.path}: duplicate key \"$name\" at $childPath" +
                                " - the last occurrence wins, the earlier value is silently discarded"
                        )
                    }
                    walk(reader, file, childPath, warnings)
                }
                reader.endObject()
            }

            JsonToken.BEGIN_ARRAY -> {
                reader.beginArray()
                var index = 0
                while (reader.hasNext()) {
                    walk(reader, file, "$path[${index++}]", warnings)
                }
                reader.endArray()
            }

            else -> reader.skipValue()
        }
    }
}

/**
 * Gradle task that validates JSON assets (prefabs, blocks, ui, etc.) at build time.
 *
 * Iterates over all configured JSON asset files and attempts to parse each one.
 * If any file cannot be parsed, the build fails with a descriptive error message.
 * See [JsonAssetInspector] for what counts as parseable, and why it is not strict JSON.
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
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val jsonAssets: ConfigurableFileCollection = project.files()

    /**
     * Where the findings are written.
     *
     * This exists mainly so the task has a declared output. Without one Gradle has no up-to-date
     * criterion and re-parses every asset on every build - which matters, because the
     * `terasology-module` plugin wires this into `processResources` for every module. With it,
     * an unchanged asset tree is skipped outright and the task can be served from the build cache.
     */
    @get:OutputFile
    val report: RegularFileProperty = project.objects.fileProperty()
        .convention(project.layout.buildDirectory.file("reports/json-assets/validation.txt"))

    /**
     * Add files or file trees of JSON assets to validate.
     */
    fun source(vararg paths: Any) {
        jsonAssets.from(*paths)
    }

    @TaskAction
    fun validate() {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        for (file in jsonAssets) {
            val inspection = JsonAssetInspector.inspect(file)
            inspection.error?.let { errors.add(it) }
            warnings.addAll(inspection.warnings)
        }

        warnings.forEach { logger.warn("  ! $it") }

        val reportFile = report.get().asFile
        reportFile.parentFile.mkdirs()
        reportFile.writeText(buildString {
            appendLine("checked: ${jsonAssets.count()}")
            appendLine("errors: ${errors.size}")
            appendLine("warnings: ${warnings.size}")
            errors.forEach { appendLine("ERROR $it") }
            warnings.forEach { appendLine("WARN $it") }
        })

        if (errors.isNotEmpty()) {
            val message = buildString {
                appendLine("Found ${errors.size} invalid JSON asset(s):")
                errors.forEach { appendLine("  - $it") }
            }
            throw GradleException(message)
        }

        logger.lifecycle("All JSON assets are valid.")
    }
}
