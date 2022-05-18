// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

plugins {
    java
    `java-library`
}

apply(from = "$rootDir/config/gradle/common.gradle")

dependencies {
    implementation("org.terasology.gestalt:gestalt-util:7.2.0-SNAPSHOT")
    implementation("org.terasology.gestalt:gestalt-module:7.2.0-SNAPSHOT")

    implementation("ch.qos.logback:logback-classic:1.2.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}