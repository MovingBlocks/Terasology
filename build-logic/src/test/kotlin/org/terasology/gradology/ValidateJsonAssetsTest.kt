// Copyright 2024 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology

import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests exercise [JsonAssetInspector] directly rather than through Gradle TestKit.
 *
 * TestKit was tried first and does not work for this task: `withPluginClasspath()` makes the
 * plugin available to the `plugins {}` DSL, but not to the build script's *compile* classpath, so
 * a generated script that does `import org.terasology.gradology.ValidateJsonAssets` fails with
 * "Unresolved reference". That failure is the worst kind - the build under test dies for a reason
 * unrelated to the assertion, so the tests never exercised the validator at all. Calling the
 * inspector directly is both honest and considerably faster.
 */
class ValidateJsonAssetsTest {

    /**
     * Verifies that the task can be registered and configured on a Gradle project.
     */
    @Test
    fun `task can be registered on a project`() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("validateJsonAssets", ValidateJsonAssets::class.java).get()
        assertNotNull(task)
        assertEquals("Verification", task.group)
    }

    /**
     * Verifies that valid JSON files pass validation without errors.
     */
    @Test
    fun `valid JSON files pass validation`() {
        val file = writeAsset("player.prefab", """
            {
              "persisted": true,
              "Location": {},
              "Network": { "replicateMode": "ALWAYS" }
            }
        """.trimIndent())

        val inspection = JsonAssetInspector.inspect(file)

        assertNull(inspection.error, "expected a clean parse, got: ${inspection.error}")
        assertTrue(inspection.warnings.isEmpty())
    }

    /**
     * Verifies that malformed JSON is reported, naming the offending file.
     */
    @Test
    fun `malformed JSON is reported as an error`() {
        val file = writeAsset("broken.prefab", """
            {
              "persisted": true,
              "Location": {
        """.trimIndent())

        val inspection = JsonAssetInspector.inspect(file)

        assertNotNull(inspection.error)
        assertTrue(
            inspection.error!!.contains("broken.prefab"),
            "Expected the error to name the file, got: ${inspection.error}"
        )
    }

    /**
     * The regression guard for this task's original defect.
     *
     * Terasology's asset format is lenient JSON, not RFC 8259. Shipped assets rely on that - a
     * licence header or an inline note is the norm, not the exception. Validating with a strict
     * parser (the task originally used org.json) fails the build on content the engine loads
     * fine: every module sampled failed, `CoreAssets` alone with 56 files.
     *
     * The fixture is modelled directly on `CoreAssets/assets/blocks/soil/Snowball.block` and
     * `CakeLie/assets/blocks/ChocolateBlock.block`.
     */
    @Test
    fun `assets using engine-style comments pass validation`() {
        val file = writeAsset("Chocolate.block", """
            /*
             * Copyright 2014 MovingBlocks
             *
             * Licensed under the Apache License, Version 2.0 (the "License");
             */
            {
                // Graphics
                "displayName": "Chocolate",
                "basedOn": "CoreAssets:soil",
                //no prefab I could find; WIP?
                "inventory": {
                    "stackable": true
                }
            }
        """.trimIndent())

        val inspection = JsonAssetInspector.inspect(file)

        assertNull(inspection.error, "comments are valid in Terasology assets, got: ${inspection.error}")
    }

    /**
     * Trailing commas are likewise accepted by the engine's lenient parser.
     * `moduleDetailsScreen.ui` has one.
     */
    @Test
    fun `trailing commas pass validation`() {
        val file = writeAsset("trailing.ui", """
            {
              "contents": [
                { "type": "UILabel" },
              ]
            }
        """.trimIndent())

        assertNull(JsonAssetInspector.inspect(file).error)
    }

    /**
     * Duplicate keys are real defects but do not break loading - Gson keeps the last occurrence -
     * so they are reported without failing the build. Found in the engine's own
     * `moduleDetailsScreen.ui`, which declared `layoutInfo` twice on one widget.
     */
    @Test
    fun `duplicate keys warn without failing`() {
        val file = writeAsset("duplicate.prefab", """
            {
              "layoutInfo": { "position-top": { "target": "TOP" } },
              "other": 1,
              "layoutInfo": { "position-top": { "target": "BOTTOM" } }
            }
        """.trimIndent())

        val inspection = JsonAssetInspector.inspect(file)

        assertNull(inspection.error, "a duplicate key must not fail the build")
        assertEquals(1, inspection.warnings.size, "expected exactly one warning: ${inspection.warnings}")
        assertTrue(inspection.warnings.single().contains("duplicate key \"layoutInfo\""))
    }

    /**
     * Duplicate keys are detected at any depth, not just at the root.
     */
    @Test
    fun `duplicate keys are detected in nested objects`() {
        val file = writeAsset("nested.prefab", """
            {
              "outer": {
                "inner": [
                  { "a": 1, "a": 2 }
                ]
              }
            }
        """.trimIndent())

        val inspection = JsonAssetInspector.inspect(file)

        assertNull(inspection.error)
        assertTrue(
            inspection.warnings.single().contains("outer.inner[0].a"),
            "Expected the warning to carry the JSON path, got: ${inspection.warnings}"
        )
    }

    /**
     * An empty asset file is a packaging mistake rather than valid content.
     */
    @Test
    fun `empty file is reported as an error`() {
        val file = writeAsset("empty.prefab", "")

        assertNotNull(JsonAssetInspector.inspect(file).error)
    }

    /**
     * A stray closing brace after the root object warns rather than failing.
     *
     * The engine's loaders read one root value and never check what follows, so the file loads
     * fine - failing the build would punish a defect the engine does not care about. Modelled on
     * `Apiculture/assets/ui/extractor.ui`, found by the first full Omega sweep.
     *
     * Note this shape makes `JsonReader.peek()` itself throw, so it is not enough to compare
     * against END_DOCUMENT; the regression here is the error/warning classification.
     */
    @Test
    fun `trailing content after the root value warns without failing`() {
        val file = writeAsset("extractor.ui", """
            {
              "type": "UIBox",
              "contents": []
            }
            }
        """.trimIndent())

        val inspection = JsonAssetInspector.inspect(file)

        assertNull(inspection.error, "the engine loads this, so it must not fail the build")
        assertTrue(
            inspection.warnings.single().contains("unexpected content after the root value"),
            "expected a trailing-content warning, got: ${inspection.warnings}"
        )
    }

    /**
     * A missing separator between object members is a hard failure - no JSON parser accepts it,
     * lenient or not, so the engine cannot load the file either.
     *
     * Modelled on `Cooking/assets/prefabs/CookingRecipes.prefab`, which is broken in the shipped
     * module: its recipes silently do not load today.
     */
    @Test
    fun `missing comma between members is an error`() {
        val file = writeAsset("CookingRecipes.prefab", """
            {
              "ListRecipes": {
                "recipes": {
                  "Cooking:Coconut": { "outputCount": 1 }
                  "Cooking:BoiledEgg": { "outputCount": 1 }
                }
              }
            }
        """.trimIndent())

        assertNotNull(
            JsonAssetInspector.inspect(file).error,
            "a missing separator must fail - the engine cannot parse it either"
        )
    }

    private fun writeAsset(name: String, content: String): File {
        val dir = Files.createTempDirectory("terasology-test").toFile()
        dir.deleteOnExit()
        return dir.resolve(name).also {
            it.writeText(content)
            it.deleteOnExit()
        }
    }
}
