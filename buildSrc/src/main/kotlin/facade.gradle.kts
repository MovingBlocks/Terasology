// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

plugins {
    application
}

val dirNatives: String by rootProject.extra

configurations {
    register("natives") {
        description = "native libraries (.dll and .so)"
    }
}

dependencies {
    "natives"(files(rootProject.file(dirNatives)).builtBy(":extractNatives"))
}
