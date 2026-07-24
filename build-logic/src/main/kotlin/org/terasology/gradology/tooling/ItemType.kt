// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology.tooling

import com.google.gson.Gson
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Properties

/**
 * Configuration for one kind of thing `gradle terasology` can manage.
 *
 * Kotlin port of the per-type config scripts under config/groovy/ (module.groovy, meta.groovy,
 * lib.groovy, facade.groovy) - each of those was a small Groovy class implementing the same shape.
 *
 * Methods take an explicit [rootDir]/[targetDir] rather than resolving relative File paths
 * themselves: this task runs inside the Gradle daemon process, whose JVM working directory is
 * *not* the project directory, so relative `File("templates/...")`-style paths resolve wrong.
 */
interface ItemType {
    /** Human-readable name used in messages, e.g. "module". */
    val itemType: String
    val excludedItems: List<String>

    /** Subdirectory name (relative to the project root) items of this type live under, e.g. "modules". */
    val targetDirName: String

    fun githubDefaultHome(properties: Properties): String
    fun findDependencies(targetDir: File, respectExcludedItems: Boolean = true): List<String>
    fun copyInTemplateFiles(rootDir: File, targetDir: File)
    fun filterItemsFromApi(possibleItems: Map<String, String?>): List<String>
    fun refreshGradle(rootDir: File, targetDir: File)

    companion object {
        val byName: Map<String, ItemType> = listOf(ModuleType, MetaType, LibType, FacadeType).associateBy { it.itemType }
    }
}

private data class ModuleDependency(val id: String? = null)
private data class ModuleTxt(val dependencies: List<ModuleDependency> = emptyList())

object ModuleType : ItemType {
    override val itemType = "module"
    override val excludedItems = listOf("engine", "Index", "out", "build")
    override val targetDirName = "modules"

    override fun githubDefaultHome(properties: Properties): String =
        properties.getProperty("alternativeGithubHome") ?: "Terasology"

    override fun findDependencies(targetDir: File, respectExcludedItems: Boolean): List<String> {
        val moduleInfo = File(targetDir, "module.txt")
        if (!moduleInfo.exists()) {
            println("The module info file did not appear to exist - can't calculate dependencies")
            return emptyList()
        }
        val config = Gson().fromJson(moduleInfo.readText(), ModuleTxt::class.java)
        val result = mutableListOf<String>()
        for (dependency in config.dependencies) {
            val id = dependency.id ?: continue
            if (respectExcludedItems && id in excludedItems) {
                println("Skipping listed dependency $id as it is in the exclude list (shipped with primary project)")
            } else {
                println("Accepting listed dependency $id")
                result += id
            }
        }
        println("Looked for dependencies, found: $result")
        return result
    }

    override fun copyInTemplateFiles(rootDir: File, targetDir: File) {
        val moduleManifest = File(targetDir, "module.txt")
        if (!moduleManifest.exists()) {
            val moduleText = File(rootDir, "templates/module.txt").readText()
            moduleManifest.appendText(moduleText.replace("MODULENAME", targetDir.name))
            println(
                "WARNING: the module ${targetDir.name} did not have a module.txt! " +
                    "One was created, please review and submit to GitHub"
            )
        }
        refreshGradle(rootDir, targetDir)
    }

    override fun filterItemsFromApi(possibleItems: Map<String, String?>): List<String> =
        possibleItems.keys.filterNot { it in excludedItems }

    override fun refreshGradle(rootDir: File, targetDir: File) {
        if (!(targetDir.canRead() && targetDir.canWrite())) {
            println("$targetDir: ⛔ not accessible")
            return
        }
        val targetPath = targetDir.toPath()
        if (Files.notExists(targetPath.resolve("module.txt"))) {
            println("$targetDir/module.txt: ❓ not present, it must not want a fresh build.gradle")
            return
        }

        val templates = File(rootDir, "templates").toPath()
        Files.copy(
            templates.resolve("build.gradle"),
            targetPath.resolve("build.gradle"),
            StandardCopyOption.REPLACE_EXISTING
        )
        println("$targetDir/build.gradle: ✨ refreshed")

        val logbackXml = targetPath.resolve("src/test/resources/logback-test.xml")
        if (Files.notExists(logbackXml)) {
            Files.createDirectories(logbackXml.parent)
            Files.copy(templates.resolve("module.logback-test.xml"), logbackXml)
            println("$logbackXml: ✨ added")
        } else {
            println("$logbackXml: already there")
        }
    }
}

object MetaType : ItemType {
    override val itemType = "meta"
    override val excludedItems = listOf("metaterasology.github.io")
    override val targetDirName = "metas"

    // Note how metas use a different override property - since same name as the paired module they
    // cannot live in the same org.
    override fun githubDefaultHome(properties: Properties): String =
        properties.getProperty("alternativeGithubMetaHome") ?: "MetaTerasology"

    // Meta modules currently do not care about dependencies.
    override fun findDependencies(targetDir: File, respectExcludedItems: Boolean): List<String> = emptyList()

    override fun copyInTemplateFiles(rootDir: File, targetDir: File) {
        val targetReadme = File(targetDir, "README.md")
        if (!targetReadme.exists()) {
            val readmeText = File(rootDir, "templates/metaREADME.markdown").readText()
            targetReadme.appendText(readmeText.replace("MODULENAME", targetDir.name))
        }
    }

    override fun filterItemsFromApi(possibleItems: Map<String, String?>): List<String> =
        possibleItems.keys.filterNot { it in excludedItems }

    override fun refreshGradle(rootDir: File, targetDir: File) {
        println("Skipping refreshGradle for meta module $targetDir - they don't Gradle")
    }
}

object LibType : ItemType {
    override val itemType = "library"
    override val excludedItems = emptyList<String>()
    override val targetDirName = "libs"

    override fun githubDefaultHome(properties: Properties): String =
        properties.getProperty("alternativeGithubHome") ?: "MovingBlocks"

    // Libs currently do not care about dependencies.
    override fun findDependencies(targetDir: File, respectExcludedItems: Boolean): List<String> = emptyList()

    // TODO: Libs don't copy anything in yet .. they might be too unique. Some may Gradle stuff but not all (like the Index)
    override fun copyInTemplateFiles(rootDir: File, targetDir: File) {}

    override fun filterItemsFromApi(possibleItems: Map<String, String?>): List<String> =
        // Libs only include repos found to have a particular string snippet in their description.
        possibleItems.filterValues { it?.contains("Automation category: Terasology Library") == true }.keys.toList()

    override fun refreshGradle(rootDir: File, targetDir: File) {
        println("Skipping refreshGradle for lib $targetDir - they vary too much to use any Gradle templates")
    }
}

object FacadeType : ItemType {
    override val itemType = "facade"
    override val excludedItems = listOf("PC")
    override val targetDirName = "facades"

    override fun githubDefaultHome(properties: Properties): String =
        properties.getProperty("alternativeGithubHome") ?: "MovingBlocks"

    // Facades currently do not care about dependencies.
    override fun findDependencies(targetDir: File, respectExcludedItems: Boolean): List<String> = emptyList()

    override fun copyInTemplateFiles(rootDir: File, targetDir: File) {
        val targetBuildGradle = File(targetDir, "build.gradle")
        if (!targetBuildGradle.exists()) {
            targetBuildGradle.appendText(File(rootDir, "templates/facades.gradle").readText())
        }
    }

    override fun filterItemsFromApi(possibleItems: Map<String, String?>): List<String> =
        // Facades only include repos found to have a particular string snippet in their description.
        possibleItems.filterValues { it?.contains("Automation category: Terasology Facade") == true }.keys.toList()

    override fun refreshGradle(rootDir: File, targetDir: File) {
        println("In refreshGradle for facade $targetDir - copying in a fresh build.gradle")
        val targetBuildGradle = File(targetDir, "build.gradle")
        targetBuildGradle.delete()
        targetBuildGradle.appendText(File(rootDir, "templates/facades.gradle").readText())
    }
}
