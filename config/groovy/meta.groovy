
class meta {

    def excludedItems = []

    def getGithubDefaultHome(Properties properties) {
        // Note how metas use a different override property - since same name as the paired module they cannot live in same org
        return properties.alternativeGithubMetaHome ?: "MetaTerasology"
    }

    def targetDirectory = "metas"
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
}
