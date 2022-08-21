// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0


import groovy.json.JsonSlurper

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class module {
    def excludedItems = ["engine", "Index", "out", "build"]

    def getGithubDefaultHome(Properties properties) {
        return properties.alternativeGithubHome ?: "Terasology"
    }

    File targetDirectory = new File("modules")
    def itemType = "module"

    String[] findDependencies(File targetDir, boolean respectExcludedItems = true) {
        def foundDependencies = readModuleDependencies(new File(targetDir, "module.txt"), respectExcludedItems)
        println "Looked for dependencies, found: " + foundDependencies
        return foundDependencies
    }

    /**
     * Reads a given module info file to figure out which if any dependencies it has. Filters out any already retrieved.
     * This method is only for modules.
     * @param targetModuleInfo the target file to check (a module.txt file or similar)
     * @return a String[] containing the next level of dependencies, if any
     */
    String[] readModuleDependencies(File targetModuleInfo, boolean respectExcludedItems = true) {
        def qualifiedDependencies = []
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

    def copyInTemplateFiles(File targetDir) {
        // Copy in the template module.txt for modules (if one doesn't exist yet)
        File moduleManifest = new File(targetDir, 'module.txt')
        if (!moduleManifest.exists()) {
            def moduleText = new File("templates/module.txt").text

            moduleManifest << moduleText.replaceAll('MODULENAME', targetDir.name)
            println "WARNING: the module ${targetDir.name} did not have a module.txt! One was created, please review and submit to GitHub"
        }

        refreshGradle(targetDir)

        // TODO: Copy in a module readme template soon
    }

    /**
     * Filters the given items based on this item type's preferences
     * @param possibleItems A map of repos (possible items) and their descriptions (potential filter data)
     * @return A list containing only the items this type cares about
     */
    List filterItemsFromApi(Map possibleItems) {
        List itemList = []

        // Modules just consider the item name and excludes those in a specific list
        itemList = possibleItems.findAll {
            !excludedItems.contains(it.key)
        }.collect { it.key }

        return itemList
    }

    def refreshGradle(File targetDir) {
        if (!(targetDir.canRead() && targetDir.canWrite())) {
            println "$targetDir: ⛔ not accessible"
            return
        }
        Path targetPath = targetDir.toPath()
        if (Files.notExists(targetPath.resolve('module.txt'))) {
            println "$targetDir/module.txt: ❓ not present, it must not want a fresh build.gradle"
            return
        }

        Path templates = Path.of('templates')
        Files.copy(templates.resolve('build.gradle'), targetPath.resolve('build.gradle'),
                StandardCopyOption.REPLACE_EXISTING)
        println "$targetDir/build.gradle: ✨ refreshed"

        Path logbackXml = targetPath.resolve('src/test/resources/logback-test.xml')
        if (Files.notExists(logbackXml)) {
            Files.createDirectories(logbackXml.parent)
            Files.copy(templates.resolve('module.logback-test.xml'), logbackXml)
            println "$logbackXml: ✨ added"
        } else {
            println "$logbackXml: already there"
        }
    }
}
