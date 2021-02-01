// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

plugins {
    application
}

val dirNatives: String by rootProject.extra

configurations {
    register("natives") {
        description = "native libraries (.dll and .so)"
    }
    register("modules") {
        description = "locally built modules and their dependencies"

        resolutionStrategy {
            preferProjectModules()
        }

        @Suppress("UnstableApiUsage")
        shouldResolveConsistentlyWith(configurations["runtimeClasspath"])
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
    dependsOn(configurations["modules"])

    destinationDir = rootProject.file("moduleCache")

    from(configurations["modules"].resolvedConfiguration.resolvedArtifacts.mapNotNull {
        if (
            (it.moduleVersion.id.group == "org.terasology.modules") and
            // FIXME: There must be a better way to filter out things from local projects than
            //    doing string comparisons on the display name.
            (!it.id.componentIdentifier.toString().startsWith("project "))
        ) it.file else null
    })
}
