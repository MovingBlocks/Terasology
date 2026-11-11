// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import org.terasology.gradology.JAR_COLLECTION
import org.terasology.gradology.moduleDependencyArtifacts
import org.terasology.gradology.moduleDependencyOrdering
import org.terasology.gradology.namedAttribute

plugins {
    id("terasology-repositories")
    `java-platform`
    `project-report`
}

@Suppress("PropertyName")
val CACHE_MODULES_DIR = rootProject.file("cachedModules")

javaPlatform {
    allowDependencies()
}

dependencies {
    // This platform depends on each of its subprojects.
    subprojects.forEach { subproject ->
        api(project(subproject.path))
    }
}

val jarCollection: Configuration = configurations.create("jarCollection") {
    description = "Provides cacheModules with JAR_COLLECTION."

    isCanBeConsumed = true
    isCanBeResolved = false

    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, namedAttribute(Usage.JAVA_RUNTIME))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, namedAttribute(JAR_COLLECTION))
    }
}

// A module built from source here must not also sit in cachedModules/ as a jar: the engine scans
// the cache before modules/ and keeps the first copy of a module id and version it meets, so a jar
// fetched while the module had no source checkout keeps shadowing the checkout after one appears,
// and changes to that module's assets never reach the game. The sync below cannot clear it: a Sync
// with nothing to copy is skipped as NO-SOURCE and leaves its destination alone.
val cleanStaleCachedModules = tasks.register<Delete>("cleanStaleCachedModules") {
    group = "build"
    description = "Removes from cachedModules/ the jars of modules that are built from source here"
    val sourceModuleNames = subprojects.map { it.name }
    delete(fileTree(CACHE_MODULES_DIR) { sourceModuleNames.forEach { include("$it-*.jar") } })
}

val fetchModuleDependencies = tasks.register<Sync>("fetchModuleDependencies") {
    group = "build"
    dependsOn(cleanStaleCachedModules)

    destinationDir = CACHE_MODULES_DIR

    val artifactsProvider = moduleDependencyArtifacts(configurations.named("classpath"))
    from(artifactsProvider.map { artifacts -> artifacts.map(ResolvedArtifactResult::getFile) })
}

val cleanFetchModuleDependencies = tasks.register<Delete>("cleanFetchModuleDependencies") {
    delete(CACHE_MODULES_DIR)
}


artifacts {
    // The output of our jarCollection configuration comes from this task.
    add(jarCollection.name, fetchModuleDependencies) {
        type = "jar-collection"
    }
}


tasks.named("clean").configure {
    dependsOn(cleanFetchModuleDependencies)

    // Allows using :modules:clean as a shortcut for running clean in each module.
    val cleanModules = this
    subprojects {
        cleanModules.dependsOn(this.tasks.named("clean"))
    }
}

val reportModuleOrder = tasks.register("reportModuleOrder") {
    moduleDependencyOrdering(configurations.getByName("classpath")).forEach {
        println(it)
    }
}
