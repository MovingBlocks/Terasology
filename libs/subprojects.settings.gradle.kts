// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

// This magically allows subdirs to become included builds
// so getting a library and build it should work, e.g.:
//    ./groovyw lib get TeraNUI
//    ./gradlew :TeraNUI:build
File(rootDir, "libs").listFiles()?.filter { it.isDirectory }?.forEach { possibleSubprojectDir ->
    val subprojectName = ":libs:" + possibleSubprojectDir.name
    val buildFile = File(possibleSubprojectDir, "build.gradle")
    val settingsFile = File(possibleSubprojectDir, "settings.gradle")
    if (!buildFile.exists()) {
        logger.warn("***** WARNING: Found a lib without a build.gradle, corrupt dir? NOT including {} *****", subprojectName)
        return@forEach
    }
    if (!settingsFile.exists()) {
        logger.warn("lib {} has build.gradle, but no settings.gradle? NOT including it.", subprojectName)
        return@forEach
    }
    logger.info("lib {} has a build file so counting it complete and including it.", subprojectName)
    includeBuild(possibleSubprojectDir)
}
