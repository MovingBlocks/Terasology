// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

dependencies {
    // Needed for caching reflected data during builds
    implementation("org.reflections:reflections:0.9.10")
    implementation("dom4j:dom4j:1.6.1")
}
