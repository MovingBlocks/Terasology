import groovy.json.JsonSlurper

class module {

    // TODO: Eventually add the Terasology org's site repo to the exclude list
    def excludedItems = ["engine", "Core", "CoreSampleGameplay", "BuilderSampleGameplay", "Index"]

    def getGithubDefaultHome(Properties properties) {
        return properties.alternativeGithubHome ?: "Terasology"
    }

    File targetDirectory = new File("modules")
    def itemType = "module"

    String[] findDependencies(File targetDir) {
        def foundDependencies = readModuleDependencies(new File(targetDir, "module.txt"))
        println "Looked for dependencies, found: " + foundDependencies
        return foundDependencies
    }

    /**
     * Reads a given module info file to figure out which if any dependencies it has. Filters out any already retrieved.
     * This method is only for modules.
     * @param targetModuleInfo the target file to check (a module.txt file or similar)
     * @return a String[] containing the next level of dependencies, if any
     */
    String[] readModuleDependencies(File targetModuleInfo) {
        def qualifiedDependencies = []
        if (!targetModuleInfo.exists()) {
            println "The module info file did not appear to exist - can't calculate dependencies"
            return qualifiedDependencies
        }
        def slurper = new JsonSlurper()
        def moduleConfig = slurper.parseText(targetModuleInfo.text)
        for (dependency in moduleConfig.dependencies) {
            if (excludedItems.contains(dependency.id)) {
                println "Skipping listed dependency $dependency.id as it is in the exclude list (shipped with primary project)"
            } else {
                println "Accepting listed dependency $dependency.id"
                qualifiedDependencies << dependency.id
            }
        }
        return qualifiedDependencies
    }

    def copyInTemplateFiles(File targetDir) {
        // Copy in the template build.gradle for modules
        println "In copyInTemplateFiles for module $targetDir.name - copying in a build.gradle then next checking for module.txt"
        File targetBuildGradle = new File(targetDir, 'build.gradle')
        targetBuildGradle.delete()
        targetBuildGradle << new File('templates/build.gradle').text

        // Copy in the template module.txt for modules (if one doesn't exist yet)
        File moduleManifest = new File(targetDir, 'module.txt')
        if (!moduleManifest.exists()) {
            def moduleText = new File("templates/module.txt").text

            moduleManifest << moduleText.replaceAll('MODULENAME', targetDir.name)
            println "WARNING: the module ${targetDir.name} did not have a module.txt! One was created, please review and submit to GitHub"
        }

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
            !excludedItems.contains (it.key)
        }.collect {it.key}

        return itemList
    }
}
