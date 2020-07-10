// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli
class ManagedModule extends ManagedItem {
    ManagedModule() {
        super()
    }

    ManagedModule(String optionGitOrigin) {
        super(optionGitOrigin)
    }

    @Override
    String getDisplayName() {
        return "module"
    }

    @Override
    File getTargetDirectory() {
        return new File("modules")
    }

    @Override
    String getDefaultItemGitOrigin() {
        return "Terasology"
    }

    /**
     * Copies in a fresh copy of build.gradle for all modules (in case changes are made and need to be propagated)
     */
    void refreshGradle() {
        targetDirectory.eachDir() { dir ->
            File targetDir = new File(targetDirectory, dir.name)

            // Copy in the template build.gradle for modules
            if (!new File(targetDir, "module.txt").exists()) {
                println "$targetDir has no module.txt, it must not want a fresh build.gradle"
                return
            }
            println "In refreshGradle for module $targetDir - copying in a fresh build.gradle"
            File targetBuildGradle = new File(targetDir, 'build.gradle')
            targetBuildGradle.delete()
            targetBuildGradle << new File('templates/build.gradle').text
        }
    }
}
