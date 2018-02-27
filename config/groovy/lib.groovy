
class lib {

    def test() {
        println "I'm the lib script!"
    }

    def excludedItems = []

    // Libs currently do not care about dependencies
    String[] findDependencies(File targetDir) {
        return []
    }
}