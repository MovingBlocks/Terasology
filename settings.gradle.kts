rootProject.name = "Terasology"

includeBuild("build-logic")
include("engine", "engine-tests", "facades", "metas", "libs", "modules")

// Handy little snippet found online that'll "fake" having nested settings.gradle files under /modules, /libs, etc
rootDir.listFiles()?.forEach { possibleSubprojectDir ->
    if (possibleSubprojectDir.isDirectory && possibleSubprojectDir.name != ".gradle") {
        possibleSubprojectDir.walkTopDown().forEach { it.listFiles { file -> 
            file.isFile && file.name == "subprojects.settings.gradle" }?.forEach { subprojectsSpecificationScript ->
                //println("Magic is happening, applying from $subprojectsSpecificationScript")
                apply {
                    from(subprojectsSpecificationScript)
                }
            }
        }
    }
}
