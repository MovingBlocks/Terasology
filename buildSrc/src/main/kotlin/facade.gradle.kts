// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import org.terasology.gradology.moduleDependencyArtifacts

plugins {
    application
}

val dirNatives: String by rootProject.extra

configurations {
    register("natives") {
        description = "native libraries (.dll and .so)"
    }
    val modulesConfig = register("modules") {
        description = "locally built modules and their dependencies"

        resolutionStrategy {
            preferProjectModules()
        }
    }
    named("compileOnly") {
        // Include modules in compileOnly so IntelliJ thinks to compile them.
        extendsFrom(modulesConfig.get())
    }
}


dependencies {
    "natives"(files(rootProject.file(dirNatives)).builtBy(":extractNatives"))

    // Make sure any local module builds are up-to-date and have their dependencies by declaring
    // a runtime dependency on whatever the `:modules` subproject declares.
    // This won't add anything if there are no modules checked out.
    "modules"(platform(project(":modules")))
}

tasks.register<Sync>("provideModuleDependencies") {
    destinationDir = rootProject.file("moduleCache")

    val modulesConfig = configurations.named("modules")
    val artifactsProvider = moduleDependencyArtifacts(modulesConfig)
    from(artifactsProvider.map { artifacts -> artifacts.map(ResolvedArtifactResult::getFile) })
}
