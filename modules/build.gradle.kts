// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    // This platform depends on each of its subprojects.
    subprojects {
        runtime(this)
    }
}
