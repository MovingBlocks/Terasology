// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import java.net.URI

plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()

    maven {
        name = "Terasology Artifactory"
        url = URI("http://artifactory.terasology.org/artifactory/virtual-repo-live")
        @Suppress("UnstableApiUsage")
        isAllowInsecureProtocol = true  // 😱
    }
}

dependencies {
    // Needed for caching reflected data during builds
    implementation("org.reflections:reflections:0.9.10")
    implementation("dom4j:dom4j:1.6.1")

    // for inspecting modules
    implementation("org.terasology:gestalt-module:5.1.5")
}
