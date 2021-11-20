// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import org.terasology.gradology.JAR_COLLECTION
import org.terasology.gradology.namedAttribute

plugins {
    application
}

val dirNatives: String by rootProject.extra

configurations {
    register("natives") {
        description = "native libraries (.dll and .so)"
    }
    register("modules") {
        description = "dependencies of locally built modules"

        attributes {
            // Specifying we need JAR_COLLECTION is how we indicate a dependency on cacheModules.
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, namedAttribute(JAR_COLLECTION))
        }
    }
}


dependencies {
    "natives"(files(rootProject.file(dirNatives)).builtBy(":extractNatives"))

    // Include modules in compileOnly so IntelliJ thinks to compile them.
    compileOnly(platform(project(":modules")))

    // Make sure all module dependencies are available to the game in cacheModules.
    "modules"(project(":modules"))
}

tasks.register<Test>("unitTest") {
    group = "Verification"
    description = "Runs unit tests (fast)"

    useJUnitPlatform {
        excludeTags("MteTest", "TteTest")
    }
    systemProperty("junit.jupiter.execution.timeout.default", "1m")
}
