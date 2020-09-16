
class meta {

    def excludedItems = ["metaterasology.github.io"]

    def getGithubDefaultHome(Properties properties) {
        // Note how metas use a different override property - since same name as the paired module they cannot live in same org
        return properties.alternativeGithubMetaHome ?: "MetaTerasology"
    }

    File targetDirectory = new File("metas")
    def itemType = "meta"

    // Meta modules currently do not care about dependencies
    String[] findDependencies(File targetDir) {
        return []
    }

    def copyInTemplateFiles(File targetDir) {
        println "In copyInTemplateFiles for meta $targetDir.name - reviewing readme template"
        File targetReadme = new File(targetDir, 'README.md')
        if (!targetReadme.exists()) {
            def readmeText = new File('templates/metaREADME.markdown').text
            targetReadme << readmeText.replaceAll('MODULENAME', targetDir.name)
        }
    }

    /**
     * Filters the given items based on this item type's preferences
     * @param possibleItems A map of repos (possible items) and their descriptions (potential filter data)
     * @return A list containing only the items this type cares about
     */
    List filterItemsFromApi(Map possibleItems) {
        List itemList = []

        // Meta modules just consider the item name and excludes those in a specific list
        itemList = possibleItems.findAll {
            !excludedItems.contains (it.key)
        }.collect {it.key}

        return itemList
    }

    def refreshGradle(File targetDir) {
        println "Skipping refreshGradle for meta module $targetDir - they don't Gradle"
    }
}
