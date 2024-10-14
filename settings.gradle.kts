rootProject.name = "Terasology"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // currently not yet for build-logic, see https://github.com/gradle/gradle/issues/15383 , change verisons
            // here and there please.
            val gestalt = version("gestalt", "8.0.0-SNAPSHOT")
            library("gestalt-core", "org.terasology.gestalt", "gestalt-asset-core" ).versionRef(gestalt)
            library("gestalt-entitysystem", "org.terasology.gestalt", "gestalt-entity-system" ).versionRef(gestalt)
            library("gestalt-inject", "org.terasology.gestalt", "gestalt-inject" ).versionRef(gestalt)
            library("gestalt-injectjava", "org.terasology.gestalt", "gestalt-inject-java" ).versionRef(gestalt)
            library("gestalt-java", "org.terasology.gestalt", "gestalt-java" ).versionRef(gestalt)
            library("gestalt-module", "org.terasology.gestalt", "gestalt-module" ).versionRef(gestalt)
            library("gestalt-util", "org.terasology.gestalt", "gestalt-util" ).versionRef(gestalt)
            library("gson", "com.google.code.gson:gson:2.8.6")
            library("guava", "com.google.guava:guava:31.1-jre")
            library("jna-platform", "net.java.dev.jna:jna-platform:5.6.0")
            val junit5 = version("junit5", "5.10.1")
            library("junit-api", "org.junit.jupiter", "junit-jupiter-api").versionRef(junit5)
            library("junit-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef(junit5)
            library("junit-params", "org.junit.jupiter", "junit-jupiter-params").versionRef(junit5)
            library("logback", "ch.qos.logback:logback-classic:1.4.14")
            val mockito = version("mockito", "5.6.0")
            library("mockito-core", "org.mockito", "mockito-core").versionRef(mockito)
            library("mockito-inline", "org.mockito:mockito-inline:3.12.4")
            library("mockito-junit", "org.mockito", "mockito-junit-jupiter").versionRef(mockito)
            // protobuf does not work as the others, see https://github.com/google/protobuf-gradle-plugin/issues/563
            val protobuf = version("protobuf", "3.22.5")
            val slf4j = version("slf4j", "2.0.11")
            library("slf4j-api", "org.slf4j", "slf4j-api").versionRef(slf4j)
            library("slf4j-jul", "org.slf4j", "jul-to-slf4j").versionRef(slf4j)
            library("slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("slf4j")
            val nui = version("nui", "4.0.0-SNAPSHOT")
            library("terasology-nui", "org.terasology.nui", "nui").versionRef(nui)
            library("terasology-nuigestalt", "org.terasology.nui", "nui-gestalt").versionRef(nui)
            library("terasology-nuireflect", "org.terasology.nui", "nui-reflect").versionRef(nui)
        }
    }
}

includeBuild("build-logic")
include("engine", "engine-tests", "facades", "metas", "libs", "modules")

// Handy little snippet found online that'll "fake" having nested settings.gradle files under /modules, /libs, etc
rootDir.listFiles()?.forEach { possibleSubprojectDir ->
    if (possibleSubprojectDir.isDirectory && possibleSubprojectDir.name != ".gradle") {
        possibleSubprojectDir.walkTopDown().forEach { it.listFiles { file -> 
            file.isFile && file.name == "subprojects.settings.gradle.kts" }?.forEach { subprojectsSpecificationScript ->
                //println("Magic is happening, applying from $subprojectsSpecificationScript")
                apply {
                    from(subprojectsSpecificationScript)
                }
            }
        }
    }
}
