// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli.managers

import groovy.json.JsonSlurper

class ManagedModule extends ManagedItem implements DependencyProvider {
    // TODO: Check these - why would they show up under modules/ ? Other than Index maybe?
    def excludedItems = ["engine", "Index", "out", "build"]

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
     * Reads a given module info file to figure out which if any dependencies it has. Filters out any already retrieved.
     * This method is only for modules.
     * @param targetModuleInfo the target file to check (a module.txt file or similar)
     * @return a String[] containing the next level of dependencies, if any
     */
    List<String> parseDependencies(File targetDirectory, String itemName, boolean respectExcludedItems = true) {
        def qualifiedDependencies = []
        File targetModuleInfo = new File(targetDirectory, itemName + "/module.txt")
        if (!targetModuleInfo.exists()) {
            println "The module info file did not appear to exist - can't calculate dependencies"
            return qualifiedDependencies
        }
        def slurper = new JsonSlurper()
        def moduleConfig = slurper.parseText(targetModuleInfo.text)
        for (dependency in moduleConfig.dependencies) {
            if (respectExcludedItems && excludedItems.contains(dependency.id)) {
                println "Skipping listed dependency $dependency.id as it is in the exclude list (shipped with primary project)"
            } else {
                println "Accepting listed dependency $dependency.id"
                qualifiedDependencies << dependency.id
            }
        }
        return qualifiedDependencies
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
