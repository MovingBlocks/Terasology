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

configure<SourceSetContainer> {
    // Adjust output path (changed with the Gradle 6 upgrade, this puts it back)
    main { java.destinationDirectory.set(layout.buildDirectory.dir("classes")) }
    test { java.destinationDirectory.set(layout.buildDirectory.dir("testClasses")) }
}

dependencies {
    implementation(libs.slf4j.api)
    implementation("net.sf.trove4j:trove4j:3.0.3")

    implementation("org.terasology:reflections:0.9.12-MB")
    implementation("org.terasology.nui:nui-reflect:4.0.0-SNAPSHOT")
    implementation(libs.gestalt.module)
    implementation(libs.gestalt.core)

    annotationProcessor(libs.gestalt.injectjava)

    testRuntimeOnly(libs.slf4j.simple) {
        because("log output during tests")
    }
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.junit)
}

tasks.register<Test>("unitTest") {
    group = "Verification"
    description = "Runs unit tests (fast)"

    useJUnitPlatform {
        excludeTags("MteTest", "TteTest")
    }
    systemProperty("junit.jupiter.execution.timeout.default", "1m")
}
