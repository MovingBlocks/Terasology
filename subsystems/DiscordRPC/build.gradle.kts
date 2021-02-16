// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

plugins {
    java
    `java-library`
}

apply(from = "$rootDir/config/gradle/common.gradle")

dependencies {
    implementation(project(":engine"))
    api("com.jagrosh:DiscordIPC:0.4")
    implementation("ch.qos.logback:logback-classic:1.2.3")
}