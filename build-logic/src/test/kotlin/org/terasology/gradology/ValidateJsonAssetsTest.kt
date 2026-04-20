// Copyright 2024 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
        val projectDir = createTempProjectDir()

        // Write a valid prefab
        val assetsDir = projectDir.resolve("assets/prefabs").also { it.mkdirs() }
        assetsDir.resolve("player.prefab").writeText("""
            {
              "persisted": true,
              "Location": {},
              "Network": { "replicateMode": "ALWAYS" }
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("validateJsonAssets")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":validateJsonAssets")?.outcome)
    }

    /**
     * Verifies that malformed JSON files cause the build to fail with a descriptive message.
     */
    @Test
    fun `malformed JSON file fails the build`() {
        val projectDir = createTempProjectDir()

        // Write an invalid prefab (missing closing brace)
        val assetsDir = projectDir.resolve("assets/prefabs").also { it.mkdirs() }
        assetsDir.resolve("broken.prefab").writeText("""
            {
              "persisted": true,
              "Location": {
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("validateJsonAssets")
            .withPluginClasspath()
            .buildAndFail()

        assertEquals(TaskOutcome.FAILED, result.task(":validateJsonAssets")?.outcome)
        assert(result.output.contains("broken.prefab")) {
            "Expected error output to mention the broken file, but got:\n${result.output}"
        }
    }

    /**
     * Verifies that a project with no assets directory succeeds without errors.
     */
    @Test
    fun `project with no assets directory passes validation`() {
        val projectDir = createTempProjectDir()
        // No assets dir created intentionally

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("validateJsonAssets")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":validateJsonAssets")?.outcome)
    }

    private fun createTempProjectDir(): File {
        val projectDir = Files.createTempDirectory("terasology-test").toFile()
        projectDir.deleteOnExit()
        // Minimal build.gradle.kts to register the task
        projectDir.resolve("build.gradle.kts").writeText("""
            import org.terasology.gradology.ValidateJsonAssets

            tasks.register<ValidateJsonAssets>("validateJsonAssets") {
                val assetsDir = project.file("assets")
                if (assetsDir.exists()) {
                    listOf("prefabs", "blocks", "ui").forEach { assetType ->
                        val dir = assetsDir.resolve(assetType)
                        if (dir.exists()) {
                            source(project.fileTree(dir) { include("**/*.json", "**/*.prefab", "**/*.block", "**/*.ui") })
                        }
                    }
                }
            }
        """.trimIndent())
        projectDir.resolve("settings.gradle.kts").writeText("")
        return projectDir
    }
}
