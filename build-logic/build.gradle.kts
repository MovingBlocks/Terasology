// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import java.net.URI

plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "4.1.0"
}

kotlin {
    jvmToolchain(11)
}

repositories {
    mavenCentral()
    google()  // gestalt uses an annotation package by Google
    gradlePluginPortal()

    maven {
        name = "Terasology Artifactory"
        url = URI("https://artifactory.terasology.io/artifactory/virtual-repo-live")
    }

    // TODO MYSTERY: As of November 7th 2021 virtual-repo-live could no longer be relied on for latest snapshots - Pro feature?
    // We've been using it that way for *years* and nothing likewise changed in the area for years as well. This seems to work ....
    maven {
        name = "Terasology snapshot locals"
        url = URI("https://artifactory.terasology.io/artifactory/terasology-snapshot-local")
    }
}

dependencies {
    implementation("org.terasology:reflections:0.9.12-MB") {
        because("reflections-manifest.gradle.kts")
    }
    // Additional corrections for old reflections dependencies:
    constraints {
        implementation("com.google.guava:guava:31.1-jre")
        implementation("org.javassist:javassist:3.29.0-GA")
    }

    // graph analysis
    implementation("org.jgrapht:jgrapht-core:1.5.0")

    // for inspecting modules
    implementation("org.terasology.gestalt:gestalt-module:7.1.0")

    // plugins we configure
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:5.1.3")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.3")

    api(kotlin("test"))
}

group = "org.terasology.gradology"
