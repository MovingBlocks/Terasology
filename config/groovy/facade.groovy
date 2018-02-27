
class facade {

    def test() {
        println "I'm the facade script!"
    }

    def excludedItems = ["PC", "TeraEd"]

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
}
