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
        return moduleConfig.dependencies.filterNotNull()
            .find { it.id.toString() == ENGINE_MODULE_NAME }
            ?.versionRange()?.toString() ?: "+"
    }

    /**
     * Dependencies declared by this module's metadata.
     *
     * @return a list of modules and whether each is optional
     */
    fun moduleDependencies(): List<Pair<GradleDependencyInfo, Boolean>> {
        val gestaltDeps = moduleConfig.dependencies.filterNotNull().filterNot { it.id.toString() == "engine" }
        return gestaltDeps.map { gradleModule(it) }
    }

    private fun gradleModule(gestaltDependency: DependencyInfo): Pair<GradleDependencyInfo, Boolean> {
        if (!gestaltDependency.minVersion.isSnapshot) {
            // gestalt considers snapshots to satisfy a minimum requirement:
            // https://github.com/MovingBlocks/gestalt/blob/fe1893821127/gestalt-module/src/main/java/org/terasology/naming/VersionRange.java#L58-L59
            gestaltDependency.minVersion = gestaltDependency.minVersion.snapshot
            // (maybe there's some way to do that with a custom gradle resolver?
            // but making a resolver that only works that way on gestalt modules specifically
            // sounds complicated.)
        }

        val version = if (gestaltDependency.versionPredicate() is VersionRange) {
            gestaltDependency.versionPredicate().toString()
        } else {
            // TODO: gradle-compatible version expressions for gestalt dependencies
            //     https://github.com/MovingBlocks/gestalt/issues/114
            gestaltDependency.minVersion.toString();
        }

        val gradleDep = GradleDependencyInfo(TERASOLOGY_MODULES_GROUP, gestaltDependency.id.toString(), version)
        return Pair(gradleDep, gestaltDependency.isOptional)
    }
}
