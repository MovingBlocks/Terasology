// This magically allows subdirs in this subproject to themselves become sub-subprojects in a proper tree structure
File(rootDir, "metas").listFiles()?.filter { it.isDirectory }?.forEach { possibleSubprojectDir ->
    if (!possibleSubprojectDir.name.startsWith(".")) {
        val subprojectName = "metas:" + possibleSubprojectDir.name
        println("Processing meta module $subprojectName, including it as a sub-project")
        include(subprojectName)
        val subprojectPath = ":" + subprojectName
        val subproject = project(subprojectPath)
        subproject.projectDir = possibleSubprojectDir
    } else {
        println("Ignoring entry $possibleSubprojectDir as it starts with .")
    }
}