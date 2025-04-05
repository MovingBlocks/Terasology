// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

// This magically allows subdirs in this subproject to themselves become sub-subprojects in a proper tree structure
File(rootDir, "modules").listFiles()?.filter { it.isDirectory }?.forEach { possibleSubprojectDir ->
    val subprojectName = ":modules:" + possibleSubprojectDir.name
    val buildFile = File(possibleSubprojectDir, "build.gradle")
    val moduleTxt = File(possibleSubprojectDir, "module.txt")
    if (!buildFile.exists()) {
        logger.warn("***** WARNING: Found a module without a build.gradle, corrupt dir? NOT including {} *****", subprojectName)
        return@forEach
    }
    if (!moduleTxt.exists()) {
        logger.warn("Module {} has build.gradle, but no module.txt? NOT including it.", subprojectName)
        return@forEach
    }
    logger.info("Module {} has a build file so counting it complete and including it.", subprojectName)
    include(subprojectName)
}
