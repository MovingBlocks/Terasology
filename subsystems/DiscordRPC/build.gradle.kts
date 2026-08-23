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
        // Upgrades for old transitive dependencies of DiscordIPC that Checkmarx doesn't like.
        // DiscordIPC itself is api (line above), so these constraints must be api too - scoping
        // them to implementation means they never actually apply to DiscordIPC's own dependency
        // graph, and the vulnerable old junixsocket 2.0.4 (with its own log4j 1.2.17) is what
        // consumers like facades:PC actually get instead of the intended upgrade.
        api("com.kohlschutter.junixsocket:junixsocket-common:2.4.0")
        api("com.kohlschutter.junixsocket:junixsocket-native-common:2.4.0")
        api("org.json:json:20220320")
    }
}
