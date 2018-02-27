
class meta {

    def excludedItems = []

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
