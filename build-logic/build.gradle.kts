// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import java.net.URI

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()  // gestalt uses an annotation package by Google

    maven {
        name = "Terasology Artifactory"
        url = URI("http://artifactory.terasology.org/artifactory/virtual-repo-live")
        @Suppress("UnstableApiUsage")
        isAllowInsecureProtocol = true  // 😱
    }

    // TODO MYSTERY: As of November 7th 2011 virtual-repo-live could no longer be relied on for latest snapshots - Pro feature?
    // We've been using it that way for *years* and nothing likewise changed in the area for years as well. This seems to work ....
    maven {
        name = "Terasology snapshot locals"
        url = URI("http://artifactory.terasology.org/artifactory/terasology-snapshot-local")
        @Suppress("UnstableApiUsage")
        isAllowInsecureProtocol = true  // 😱
    }
}

dependencies {
    // Needed for caching reflected data during builds
    implementation("org.terasology:reflections:0.9.12-MB")
    implementation("org.javassist:javassist:3.27.0-GA")
    implementation("dom4j:dom4j:1.6.1")

    // graph analysis
    implementation("org.jgrapht:jgrapht-core:1.5.0")

    // for inspecting modules
    implementation("org.terasology.gestalt:gestalt-module:7.1.0")

    api(kotlin("test"))
}

group = "org.terasology.gradology"
