// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

plugins {
    java
    `java-library`
    id("terasology-common")
}

apply(from = "$rootDir/config/gradle/publish.gradle")

group = "org.terasology.subsystems"
version = project(":engine").version

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("net.sf.trove4j:trove4j:3.0.3")

    implementation("org.terasology:reflections:0.9.12-MB")
    implementation("org.terasology.nui:nui-reflect:3.0.0")
    implementation("org.terasology.gestalt:gestalt-module:7.1.0")
    implementation("org.terasology.gestalt:gestalt-asset-core:7.1.0")

    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.32") {
        because("log output during tests")
    }
    testImplementation(platform("org.junit:junit-bom:5.8.1")) {
        // junit-bom will set version numbers for the other org.junit dependencies.
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.mockito:mockito-inline:3.12.4")

    testImplementation("org.mockito:mockito-junit-jupiter:3.12.4")
}

tasks.register<Test>("unitTest") {
    group = "Verification"
    description = "Runs unit tests (fast)"

    useJUnitPlatform {
        excludeTags("MteTest", "TteTest")
    }
    systemProperty("junit.jupiter.execution.timeout.default", "1m")
}
