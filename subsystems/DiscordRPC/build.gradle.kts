// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

plugins {
    java
    `java-library`
    id("terasology-common")
}

apply(from = "$rootDir/config/gradle/common.gradle")

configure<SourceSetContainer> {
    // Adjust output path (changed with the Gradle 6 upgrade, this puts it back)
    main { java.destinationDirectory.set(layout.buildDirectory.dir("classes")) }
    test { java.destinationDirectory.set(layout.buildDirectory.dir("testClasses")) }
}

dependencies {
    implementation(project(":engine"))

    annotationProcessor(libs.gestalt.injectjava)

    api("com.jagrosh:DiscordIPC:0.4")

    constraints {
        // Upgrades for old transitive dependencies of DiscordIPC that Checkmarx doesn't like
        implementation("com.kohlschutter.junixsocket:junixsocket-common:2.4.0")
        implementation("com.kohlschutter.junixsocket:junixsocket-native-common:2.4.0")
        implementation("org.json:json:20220320")
    }
}
