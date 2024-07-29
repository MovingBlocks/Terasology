// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology

import org.gradle.api.Project
import org.terasology.gestalt.module.ModuleMetadata
import org.terasology.gestalt.module.ModuleMetadataJsonAdapter
import org.terasology.gestalt.module.dependencyresolution.DependencyInfo
import org.terasology.gestalt.naming.Version
import org.terasology.gestalt.naming.VersionRange
import java.io.File


// This might be a gradle ExternalModuleDependency or ModuleComponentSelector
// or something else from gradle.api.artifacts, but it exposes no concrete implementation
// of those interfaces.
data class GradleDependencyInfo(val group: String, val module: String, val version: String) {
    fun asMap(): Map<String, String> {
        return mapOf("group" to group, "name" to module, "version" to version)
    }
}


class ModuleMetadataForGradle(private val moduleConfig: ModuleMetadata) {

    companion object {
        fun fromFile(moduleFile: File, project: Project?): ModuleMetadataForGradle {
            val moduleConfig = try {
                moduleFile.reader().use {
                    ModuleMetadataJsonAdapter().read(it)!!
                }
            } catch (e: Exception) {
                throw ModuleInfoException(e, moduleFile, project)
            }

            return ModuleMetadataForGradle(moduleConfig)
        }

        fun forProject(project: Project): ModuleMetadataForGradle {
            return fromFile(project.file(MODULE_INFO_FILENAME), project)
        }
    }

    val version: Version
        get() = moduleConfig.version

    val group: String = TERASOLOGY_MODULES_GROUP

    fun engineVersion(): String {
        return moduleConfig.dependencies
            .filterNotNull()
            .find { it.id.toString() == ENGINE_MODULE_NAME }
            ?.let(this::versionStringFromGestaltDependency)
            ?: "+"
    }

    /**
     * Dependencies declared by this module's metadata.
     *
     * @return a list of modules and whether each is optional
     */
    fun moduleDependencies(): List<Pair<GradleDependencyInfo, Boolean>> {
        val gestaltDeps = moduleConfig.dependencies.filterNotNull().filterNot { it.id.toString() == ENGINE_MODULE_NAME }
        return gestaltDeps.map { gradleModule(it) }
    }

    private fun versionStringFromGestaltDependency(gestaltDependency: DependencyInfo): String {
        val version = if (gestaltDependency.versionRange() is VersionRange) {
            gestaltDependency.versionRange().toString()
        } else {
            // TODO: gradle-compatible version expressions for gestalt dependencies
            //     https://github.com/MovingBlocks/gestalt/issues/114
            "[${gestaltDependency.minVersion},)"                                       
        }
        return version;
    }

    private fun gradleModule(gestaltDependency: DependencyInfo): Pair<GradleDependencyInfo, Boolean> {
        val version = versionStringFromGestaltDependency(gestaltDependency)

        val gradleDep = GradleDependencyInfo(TERASOLOGY_MODULES_GROUP, gestaltDependency.id.toString(), version)
        return Pair(gradleDep, gestaltDependency.isOptional)
    }
}
