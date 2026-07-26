// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

// This magically allows subdirs to become included builds
// so getting a library and build it should work, e.g.:
//    ./groovyw lib get TeraNUI
//    ./gradlew :TeraNUI:build
File(rootDir, "libs").listFiles()?.filter { it.isDirectory }?.forEach { possibleSubprojectDir ->
    val subprojectName = ":libs:" + possibleSubprojectDir.name
    val buildFileExists = File(possibleSubprojectDir, "build.gradle").exists() ||
        File(possibleSubprojectDir, "build.gradle.kts").exists()
    val settingsFileExists = File(possibleSubprojectDir, "settings.gradle").exists() ||
        File(possibleSubprojectDir, "settings.gradle.kts").exists()
    if (!buildFileExists) {
        logger.warn("***** WARNING: Found a lib without a build.gradle(.kts), corrupt dir? NOT including {} *****", subprojectName)
        return@forEach
    }
    if (!settingsFileExists) {
        logger.warn("lib {} has build.gradle(.kts), but no settings.gradle(.kts)? NOT including it.", subprojectName)
        return@forEach
    }
    logger.info("lib {} has a build file so counting it complete and including it.", subprojectName)
    includeBuild(possibleSubprojectDir)
}
