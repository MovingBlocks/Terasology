// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

// Simple build file for modules - the one under the Core module is the template, will be copied as needed to modules

import org.gradle.plugins.ide.eclipse.model.EclipseModel
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.terasology.gradology.ModuleMetadataForGradle

plugins {
    `java-library`
    idea
    eclipse
    id("terasology-common")
}

val moduleMetadata = ModuleMetadataForGradle.forProject(project)

project.version = moduleMetadata.version
// Jenkins-Artifactory integration catches on to this as part of the Maven-type descriptor
project.group = moduleMetadata.group

logger.info("Version for {} loaded as {} for group {}", project.name, project.version, project.group)

// Grab all the common stuff like plugins to use, artifact repositories, code analysis config, Artifactory settings, Git magic
// Note that this statement is down a ways because it is affected by the stuff higher in this file like setting versioning
apply(from = "$rootDir/config/gradle/publish.gradle")

// Handle some logic related to where what is
configure<SourceSetContainer> {
    main {
        java.destinationDirectory.set(layout.buildDirectory.dir("classes"))
    }
    test {
        java.destinationDirectory.set(layout.buildDirectory.dir("testClasses"))
    }
}

configurations {
    all {
        resolutionStrategy {
            preferProjectModules()
            // always pick reflections fork
            dependencySubstitution {
                @Suppress("UnstableApiUsage")
                substitute(module("org.reflections:reflections")).using(module("org.terasology:reflections:0.9.12-MB"))
            }
        }
    }
}

// Set dependencies. Note that the dependency information from module.txt is used for other Terasology modules
dependencies {
    implementation(group = "org.terasology.engine", name = "engine", version = moduleMetadata.engineVersion())
    testImplementation(group = "org.terasology.engine", name = "engine-tests", version = moduleMetadata.engineVersion())

    annotationProcessor("org.terasology.gestalt:gestalt-inject-java:8.0.0-SNAPSHOT")

    for ((gradleDep, optional) in moduleMetadata.moduleDependencies()) {
        if (optional) {
            // `optional` module dependencies are ones it does not require for runtime
            // (but will use opportunistically if available)
            compileOnly(gradleDep.asMap())
            // though modules also sometimes use "optional" to describe their test dependencies;
            // they're not required for runtime, but they *are* required for tests.
            testImplementation(gradleDep.asMap())
        } else {
            implementation(gradleDep.asMap())
        }
    }

    // see terasology-metrics for test dependencies
}


if (project.name == "ModuleTestingEnvironment") {
    dependencies {
        // MTE is a special snowflake, it gets these things as non-test dependencies
        implementation(group = "org.terasology.engine", name = "engine-tests", version = moduleMetadata.engineVersion())
        implementation("ch.qos.logback:logback-classic:1.4.14")
        runtimeOnly("org.codehaus.janino:janino:3.1.3") {
            because("logback filters")
        }
        add("implementation", platform("org.junit:junit-bom:5.10.1"))
        implementation("org.junit.jupiter:junit-jupiter-api")
        implementation("org.mockito:mockito-junit-jupiter:3.12.4")
    }
}


// Generate the module directory structure if missing
tasks.register("createSkeleton") {
    mkdir("assets")
    mkdir("assets/animations")
    mkdir("assets/atlas")
    mkdir("assets/behaviors")
    mkdir("assets/blocks")
    mkdir("assets/blockSounds")
    mkdir("assets/blockTiles")
    mkdir("assets/fonts")
    mkdir("assets/i18n")
    mkdir("assets/materials")
    mkdir("assets/mesh")
    mkdir("assets/music")
    mkdir("assets/prefabs")
    mkdir("assets/shaders")
    mkdir("assets/shapes")
    mkdir("assets/skeletalMesh")
    mkdir("assets/skins")
    mkdir("assets/sounds")
    mkdir("assets/textures")
    mkdir("assets/ui")
    mkdir("overrides")
    mkdir("deltas")
    mkdir("src/main/java")
    mkdir("src/test/java")
}


val mainSourceSet: SourceSet = sourceSets[SourceSet.MAIN_SOURCE_SET_NAME]


// This task syncs everything in the assets dir into the output dir, used when jarring the module
tasks.register<Sync>("syncAssets") {
    from("assets")
    into("${mainSourceSet.output.classesDirs.first()}/assets")
}

tasks.register<Sync>("syncOverrides") {
    from("overrides")
    into("${mainSourceSet.output.classesDirs.first()}/overrides")
}

tasks.register<Sync>("syncDeltas") {
    from("deltas")
    into("${mainSourceSet.output.classesDirs.first()}/deltas")
}

tasks.register<Copy>("syncModuleInfo") {
    from("module.txt")
    into(mainSourceSet.output.classesDirs.first())
}

tasks.named("processResources") {
    // Make sure the assets directory is included
    dependsOn("syncAssets", "syncOverrides", "syncDeltas", "syncModuleInfo")
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn("processResources")
    // Create an asset list during compilation (needed for Gestalt 8)
    inputs.files(sourceSets.main.get().resources.srcDirs)
    options.compilerArgs = arrayListOf("-Aresource=${sourceSets.main.get().resources.srcDirs.joinToString(File.pathSeparator)}")
}
tasks.named<JavaCompile>("compileTestJava") {
    dependsOn("processResources")
    // Create an asset list during compilation (needed for Gestalt 8)
    inputs.files(sourceSets.test.get().resources.srcDirs)
    options.compilerArgs = arrayListOf("-Aresource=${sourceSets.test.get().resources.srcDirs.joinToString(File.pathSeparator)}")
}

tasks.named<Test>("test") {
    description = "Runs all tests (slow)"
    useJUnitPlatform ()
    systemProperty("junit.jupiter.execution.timeout.default", "4m")
}

tasks.register<Test>("unitTest") {
    group =  "Verification"
    description = "Runs unit tests (fast)"
    useJUnitPlatform {
        excludeTags = setOf("MteTest", "TteTest")
    }
    systemProperty("junit.jupiter.execution.timeout.default", "1m")
}

tasks.register<Test>("integrationTest") {
    group = "Verification"
    description = "Runs integration tests (slow) tagged with 'MteTest' or 'TteTest'"

    useJUnitPlatform {
        includeTags = setOf("MteTest", "TteTest")
    }
    systemProperty("junit.jupiter.execution.timeout.default", "4m")
}

// Prep an IntelliJ module for the Terasology module - yes, might want to read that twice :D
configure<IdeaModel> {
    module {
        // Change around the output a bit
        inheritOutputDirs = false
        outputDir = layout.buildDirectory.dir("classes").get().asFile
        testOutputDir = layout.buildDirectory.dir("testClasses").get().asFile
        isDownloadSources = true
    }
}

// For Eclipse just make sure the classpath is right
configure<EclipseModel> {
    classpath {
        defaultOutputDir = file("build/classes")
    }
}
