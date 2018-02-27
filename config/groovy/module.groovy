import groovy.json.JsonSlurper

class module {

    def test() {
        println "I'm the module script!"
    }

    def excludedItems = ["engine", "Core", "CoreSampleGameplay", "BuilderSampleGameplay"]


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
                println "Skipping listed dependency $dependency as it is in the exclude list (shipped with primary project)"
            } else {
                println "Accepting listed dependency $dependency"
                qualifiedDependencies << dependency.id
            }
        }
        return qualifiedDependencies
    }

}

// TODO: Move the module.txt parsing for dependencies in here since it is unique to this type
// Make the function methods call into the types for extra steps like what files to copy into a new dir
// shift MODULENAME to ITEMNAMEREPLACEMENT
// make a list of template files to copy
// replace ITEMNAMEREPLACEMENT in each
