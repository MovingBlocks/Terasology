// Copyright 2020 The Terasology Foundation
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

    implementation("org.reflections:reflections:0.9.10")
    implementation("org.terasology.nui:nui-reflect:1.3.1")
    implementation("org.terasology:gestalt-module:5.1.5")
    implementation("org.terasology:gestalt-asset-core:5.1.5")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.5.2")
    testImplementation("org.mockito:mockito-junit-jupiter:3.2.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}
