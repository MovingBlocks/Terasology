// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Provider
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.traverse.TopologicalOrderIterator

val ModuleIdentifier.isTerasologyModule: Boolean
    get() = group == TERASOLOGY_MODULES_GROUP

private val logger: Logger = Logging.getLogger("org.tersology.gradology.module_deps")


/**
 * Retrieve module dependencies.
 *
 * Finds dependencies of this configuration which are Terasology modules
 * and are not provided by local projects.
 */
fun moduleDependencyArtifacts(modulesConfig: Provider<Configuration>): Provider<Iterable<ResolvedArtifactResult>> =
    modulesConfig.map {
        configuration -> moduleDependencyArtifacts(configuration)
    }


/**
 * Retrieve module dependencies.
 *
 * Finds dependencies of this configuration which are Terasology modules
 * and are not provided by local projects.
 */
fun moduleDependencyArtifacts(modulesConfig: Configuration): Iterable<ResolvedArtifactResult> {
    // configurations.resolvedConfiguration is more straightforward if you just want all the artifacts,
    // but using `.incoming` lets us turn on lenient mode as well as do more accurate filtering of local modules
    val resolvable = modulesConfig.incoming
    val artifactView = resolvable.artifactView {
        lenient(true)
    }

    val allDependencies = resolvable.resolutionResult.allDependencies
    val resolvedDependencies = allDependencies.mapNotNull {
        if (it is ResolvedDependencyResult) {
            return@mapNotNull it
        }
        if (it is UnresolvedDependencyResult) {
            logger.warn("Dependency {} of {} not resolved:", it.attempted, it.from, it.failure)
        } else {
            logger.warn(
                "Dependency {} of {} not resolved: Unexpected result class {}\n{}",
                it.requested,
                it.from,
                it::class,
                it
            )
        }
        null
    }
    val moduleIdentifiers: Set<ModuleIdentifier> = resolvedDependencies.mapNotNull { dependency ->
        val moduleId = when (val selectedId = dependency.selected.id) {
            is ProjectComponentIdentifier -> null  // local source
            is ModuleComponentIdentifier -> selectedId.moduleIdentifier
            else -> error("What type is ${selectedId::class}?")
        }
        moduleId?.takeIf { it.isTerasologyModule }
    }.toSet()

    val moduleArtifacts = artifactView.artifacts.filter { artifact ->
        val moduleId = (artifact.id.componentIdentifier as? ModuleComponentIdentifier)?.moduleIdentifier
        moduleId != null && moduleIdentifiers.contains(moduleId)
    }
    logger.debug("Resolved artifacts for {} modules.", { moduleIdentifiers.size })
    return moduleArtifacts
}


fun moduleDependencyOrdering(modulesConfig: Configuration): List<String> {
    // configurations.resolvedConfiguration is more straightforward if you just want all the artifacts,
    // but using `.incoming` lets us turn on lenient mode as well as do more accurate filtering of local modules
    val resolvable = modulesConfig.incoming
    resolvable.artifactView {
        lenient(true)
    }

    val result = resolvable.resolutionResult
    val allDependencies = result.allDependencies
    allDependencies.mapNotNull {
        if (it is ResolvedDependencyResult) {
            return@mapNotNull it
        }
        if (it is UnresolvedDependencyResult) {
            logger.warn("Dependency {} of {} not resolved:", it.attempted, it.from, it.failure)
        } else {
            logger.warn(
                "Dependency {} of {} not resolved: Unexpected result class {}\n{}",
                it.requested,
                it.from,
                it::class,
                it
            )
        }
        null
    }

    val g: Graph<ResolvedComponentResult, ResolvedDependencyResult> = DefaultDirectedGraph(ResolvedDependencyResult::class.java)


    fun isModule(p: ResolvedComponentResult): Boolean {
        val projectPath = (p.id as? ProjectComponentIdentifier)?.projectPath
        return projectPath?.run {
            startsWith(":modules:") || equals(":engine")
        } ?: false
    }

    result.allComponents
        .filter(::isModule)
        .forEach { g.addVertex(it) }

    result.allDependencies
        .mapNotNull {
            it as? ResolvedDependencyResult
        }
        .filter {
            isModule(it.from) && isModule(it.selected)
        }
        .forEach {
            g.addEdge(it.from, it.selected, it)
        }

    val nodes = TopologicalOrderIterator(g)
    return nodes.asSequence()
        .mapNotNull { it.id as? ProjectComponentIdentifier }
        .map { it.projectName }
        .toList().asReversed()
}
