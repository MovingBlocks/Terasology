// Copyright 2024 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.MalformedJsonException
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
 * The contract that follows from this: **an error is something the engine genuinely cannot load;
 * a warning is something it loads despite the file being defective.** Duplicate keys and trailing
 * content are both warnings for that reason - Gson keeps the last duplicate, and the loaders read
 * a single root value without checking what comes after it. Anything that fails the parse outright
 * fails the build, because the engine would fail on it too.
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
                    return AssetInspection("file is empty", warnings)
                }

                walk(reader, "", warnings)

                // Content after the root value is a defect, but not a fatal one: the engine's
                // loaders read a single value and never check what follows, so the file still
                // loads. Peeking can itself throw when the trailing bytes are not the start of a
                // value (a stray closing brace, say), so that has to be caught here rather than
                // by the outer handler - otherwise it would be reported as a parse failure.
                val hasTrailingContent = try {
                    reader.peek() != JsonToken.END_DOCUMENT
                } catch (e: MalformedJsonException) {
                    // Only malformed trailing bytes count as trailing content. A read failure is
                    // not a verdict about the file, so it falls through to the outer handler and
                    // is reported as an error rather than being swallowed as a passing warning.
                    true
                }

                if (hasTrailingContent) {
                    warnings.add(
                        "unexpected content after the root value" +
                            " - the engine reads the first value and silently ignores the rest"
                    )
                }
            }
            AssetInspection(null, warnings)
        } catch (e: Exception) {
            AssetInspection(e.message ?: e.javaClass.simpleName, warnings)
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
    private fun walk(reader: JsonReader, path: String, warnings: MutableList<String>) {
        when (reader.peek()) {
            JsonToken.BEGIN_OBJECT -> {
                reader.beginObject()
                val seen = mutableSetOf<String>()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    val childPath = if (path.isEmpty()) name else "$path.$name"
                    if (!seen.add(name)) {
                        warnings.add(
                            "duplicate key \"$name\" at $childPath" +
                                " - the last occurrence wins, the earlier value is silently discarded"
                        )
                    }
                    walk(reader, childPath, warnings)
                }
                reader.endObject()
            }

            JsonToken.BEGIN_ARRAY -> {
                reader.beginArray()
                var index = 0
                while (reader.hasNext()) {
                    walk(reader, "$path[${index++}]", warnings)
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
     * Where the findings are written, in CheckStyle XML.
     *
     * Two jobs. It gives the task a declared output, without which Gradle has no up-to-date
     * criterion and re-parses every asset on every build — that matters because the
     * `terasology-module` plugin wires this into `processResources` for every module.
     *
     * And it makes the findings *visible*. Warnings that only reach the console are warnings
     * nobody reads. CheckStyle XML is the format the Jenkinsfile already parses with the Warnings
     * Next Generation plugin, so recording these alongside checkstyle, PMD and SpotBugs is one
     * more `recordIssues` block rather than new infrastructure — including the reference-build
     * comparison that distinguishes a warning you just introduced from the existing backlog.
     *
     * Deliberately not `@CacheableTask`: findings name files by absolute path, as every other
     * analysis report here does, so the output is not relocatable between machines.
     */
    @get:OutputFile
    val report: RegularFileProperty = project.objects.fileProperty()
        .convention(project.layout.buildDirectory.file("reports/json-assets/json-assets.xml"))

    /**
     * Add files or file trees of JSON assets to validate.
     */
    fun source(vararg paths: Any) {
        jsonAssets.from(*paths)
    }

    @TaskAction
    fun validate() {
        val errors = mutableListOf<String>()
        val findings = LinkedHashMap<File, MutableList<Pair<String, String>>>()

        for (file in jsonAssets) {
            val inspection = JsonAssetInspector.inspect(file)
            val perFile = findings.getOrPut(file) { mutableListOf() }
            inspection.error?.let {
                errors.add("${file.path}: $it")
                perFile.add("error" to it)
            }
            inspection.warnings.forEach {
                logger.warn("  ! ${file.path}: $it")
                perFile.add("warning" to it)
            }
            if (perFile.isEmpty()) {
                findings.remove(file)
            }
        }

        writeCheckstyleReport(findings)

        if (errors.isNotEmpty()) {
            val message = buildString {
                appendLine("Found ${errors.size} invalid JSON asset(s):")
                errors.forEach { appendLine("  - $it") }
            }
            throw GradleException(message)
        }

        logger.lifecycle("All JSON assets are valid.")
    }

    /**
     * Emit findings as CheckStyle XML — the schema the Warnings Next Generation plugin already
     * parses for this project. Severity carries the contract: `error` is something the engine
     * cannot load, `warning` is something it loads despite the file being defective.
     *
     * No line numbers: Gson's reader reports a JSON path rather than a position, so the path is
     * put in the message instead and the line is left at 0. Grouping and trend still work; only
     * inline annotation precision is lost.
     */
    private fun writeCheckstyleReport(findings: Map<File, List<Pair<String, String>>>) {
        val reportFile = report.get().asFile
        reportFile.parentFile.mkdirs()
        reportFile.writeText(buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<checkstyle version="8.0">""")
            findings.forEach { (file, entries) ->
                appendLine("""  <file name="${xmlAttr(file.path)}">""")
                entries.forEach { (severity, message) ->
                    appendLine(
                        """    <error line="0" severity="$severity" """ +
                            """message="${xmlAttr(message)}" source="JsonAssets" />"""
                    )
                }
                appendLine("  </file>")
            }
            appendLine("</checkstyle>")
        })
    }

    /** Escape the five characters that cannot appear literally in an XML attribute value. */
    private fun xmlAttr(value: String): String =
        value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
}
