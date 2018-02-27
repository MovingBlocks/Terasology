
class lib {

    def excludedItems = []

    // Libs currently do not care about dependencies
    String[] findDependencies(File targetDir) {
        return []
    }

    // TODO: Libs don't copy anything in yet .. they might be too unique. Some may Gradle stuff but not all (like the Index)
    def copyInTemplateFiles(File targetDir) {

    }
}
