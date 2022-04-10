
class lib {

    def excludedItems = []

    def getGithubDefaultHome(Properties properties) {
        return properties.alternativeGithubHome ?: "MovingBlocks"
    }

    File targetDirectory = new File("libs")
    def itemType = "library"

    // Libs currently do not care about dependencies
    String[] findDependencies(File targetDir) {
        return []
    }

    // TODO: Libs don't copy anything in yet .. they might be too unique. Some may Gradle stuff but not all (like the Index)
    def copyInTemplateFiles(File targetDir) {

    }

    /**
     * Filters the given items based on this item type's preferences
     * @param possibleItems A map of repos (possible items) and their descriptions (potential filter data)
     * @return A list containing only the items this type cares about
     */
    List filterItemsFromApi(Map possibleItems) {
        List itemList = []

        // Libs only includes repos found to have a particular string snippet in their description
        // TODO: Consideration for libraries - generic vs project specific? TeraMath could be used in DestSol etc ...
        itemList = possibleItems.findAll {
            it.value?.contains("Automation category: Terasology Library")
        }.collect {it.key}

        return itemList
    }

    def refreshGradle(File targetDir) {
        println "Skipping refreshGradle for lib $targetDir- they vary too much to use any Gradle templates"
    }
}
