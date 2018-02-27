
class meta {

    def test() {
        println "I'm the meta script!"
    }

    def excludedItems = []

    // Meta modules currently do not care about dependencies
    String[] findDependencies(File targetDir) {
        return []
    }
}