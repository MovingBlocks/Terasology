
class facade {

    def test() {
        println "I'm the facade script!"
    }

    def excludedItems = ["PC", "TeraEd"]

    // Facades currently do not care about dependencies
    String[] findDependencies(File targetDir) {
        return []
    }
}