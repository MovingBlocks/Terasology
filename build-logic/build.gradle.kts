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
}

dependencies {
    // Needed for caching reflected data during builds
    implementation("org.terasology:reflections:0.9.12-MB")
    implementation("org.javassist:javassist:3.27.0-GA")
    implementation("dom4j:dom4j:1.6.1")

    // for inspecting modules
    implementation("org.terasology.gestalt:gestalt-module:7.1.0")

    api(kotlin("test"))
}

group = "org.terasology.gradology"
