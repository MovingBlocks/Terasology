// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

plugins {
    java
    `java-library`
}

apply(from = "$rootDir/config/gradle/common.gradle")

dependencies {
    api("com.google.code.gson:gson:2.6.2")

    implementation(project(":subsystems:TypeHandlerLibrary"))
    
    implementation("com.google.guava:guava:23.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.5.2")
    testImplementation("org.mockito:mockito-junit-jupiter:3.2.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}