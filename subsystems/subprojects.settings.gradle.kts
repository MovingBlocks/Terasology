// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

// This magically allows subdirs in this subproject to themselves become sub-subprojects in a proper tree structure
File(rootDir, "subsystems").listFiles()?.filter { it.isDirectory }?.forEach { possibleSubprojectDir ->
    if (!possibleSubprojectDir.name.startsWith(".")) {
        val subprojectName = "subsystems:" + possibleSubprojectDir.name
        logger.info("Including '{}' as a sub-project.", subprojectName)
        include(subprojectName)
        val subprojectPath = ":" + subprojectName
        val subproject = project(subprojectPath)
        subproject.projectDir = possibleSubprojectDir
    } else {
        logger.info("Ignoring hidden folder '{}'.", possibleSubprojectDir)
    }
}