// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

plugins {
    java
    `java-library`
}

apply(from = "$rootDir/config/gradle/publish.gradle")

group = "org.terasology.subsystems"
version = project(":engine").version

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.21")
    implementation("net.sf.trove4j:trove4j:3.0.3")

    implementation("org.terasology:reflections:0.9.12-MB")
    implementation("org.terasology.nui:nui-reflect:3.0.0")
    implementation("org.terasology.gestalt:gestalt-module:7.1.0")
    implementation("org.terasology.gestalt:gestalt-asset-core:7.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.5.2")
    testImplementation("org.mockito:mockito-junit-jupiter:3.11.2")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}
