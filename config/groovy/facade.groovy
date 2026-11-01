
class facade {

    def excludedItems = ["PC"]

    def getGithubDefaultHome(Properties properties) {
        return properties.alternativeGithubHome ?: "MovingBlocks"
    }

    File targetDirectory = new File("facades")
    def itemType = "facade"

    // Facades currently do not care about dependencies
    String[] findDependencies(File targetDir) {
        return []
    }

    def copyInTemplateFiles(File targetDir) {
        println "In copyInTemplateFiles for facade $targetDir.name - reviewing Gradle needs"
        File targetBuildGradle = new File(targetDir, 'build.gradle')
        if (!targetBuildGradle.exists()) {
            targetBuildGradle << new File('templates/facades.gradle').text
        }
    }

    /**
     * Filters the given items based on this item type's preferences
     * @param possibleItems A map of repos (possible items) and their descriptions (potential filter data)
     * @return A list containing only the items this type cares about
     */
    List filterItemsFromApi(Map possibleItems) {
        List itemList = []

        // Facades only includes repos found to have a particular string snippet in their description
        itemList = possibleItems.findAll {
            it.value?.contains("Automation category: Terasology Facade")
        }.collect {it.key}

        return itemList
    }

    def refreshGradle(File targetDir) {
        // Copy in the template build.gradle for facades
        println "In refreshGradle for facade $targetDir - copying in a fresh build.gradle"
        File targetBuildGradle = new File(targetDir, 'build.gradle')
        targetBuildGradle.delete()
        targetBuildGradle << new File('templates/facades.gradle').text
    }
}
