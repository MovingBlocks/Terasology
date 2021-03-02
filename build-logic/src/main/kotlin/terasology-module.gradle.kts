// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

// Simple build file for modules - the one under the Core module is the template, will be copied as needed to modules

import org.gradle.plugins.ide.eclipse.model.EclipseModel
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import org.terasology.gradology.ModuleMetadataForGradle

plugins {
    `java-library`
    idea
    eclipse
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
        java.outputDir = buildDir.resolve("classes")
    }
    test {
        java.outputDir = buildDir.resolve("testClasses")
    }
}

configurations {
    all {
        resolutionStrategy.preferProjectModules()
    }
}

// Set dependencies. Note that the dependency information from module.txt is used for other Terasology modules
dependencies {
    implementation(group = "org.terasology.engine", name = "engine", version = moduleMetadata.engineVersion())
    implementation(group = "org.terasology.engine", name = "engine-tests", version = moduleMetadata.engineVersion())

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

    testRuntimeOnly("org.slf4j:jul-to-slf4j:1.7.21")

    add("testImplementation", platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("org.mockito:mockito-junit-jupiter:3.7.7")

    //backwards compatibility with modules tests
    testImplementation("junit:junit:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter-migrationsupport")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}


if (project.name == "ModuleTestingEnvironment") {
    dependencies {
        // MTE is a special snowflake, it gets these things as non-test dependencies
        implementation("ch.qos.logback:logback-classic:1.2.3")
        runtimeOnly("org.codehaus.janino:janino:3.1.3") {
            because("logback filters")
        }
        add("implementation", platform("org.junit:junit-bom:5.7.1"))
        implementation("org.junit.jupiter:junit-jupiter-api")
        implementation("org.mockito:mockito-junit-jupiter:3.7.7")
        implementation("junit:junit:4.13.1")
        //TODO: Remove shrinkwrap from code, you have FileSystem in java 8
        implementation("org.jboss.shrinkwrap:shrinkwrap-depchain-java7:1.2.1")
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


tasks.register("cacheReflections") {
    description = "Caches reflection output to make regular startup faster. May go stale and need cleanup at times."
    inputs.files(mainSourceSet.output.classesDirs)
    outputs.file(File(mainSourceSet.output.classesDirs.first(), "reflections.cache"))
    dependsOn(tasks.named("classes"))

    outputs.upToDateWhen { tasks.named("classes").get().state.upToDate }

    doFirst {
        try {
            val reflections = Reflections(ConfigurationBuilder()
                    .filterInputsBy(FilterBuilder.parsePackages("+org"))
                    .addUrls(inputs.getFiles().getSingleFile().toURI().toURL())
                    .setScanners(TypeAnnotationsScanner(), SubTypesScanner()))
            reflections.save(outputs.getFiles().getAsPath())
        } catch (e: java.net.MalformedURLException) {
            logger.error("Cannot parse input to url", e)
        }
    }
}

tasks.register<Delete>("cleanReflections") {
    description = "Cleans the reflection cache. Useful in cases where it has gone stale and needs regeneration."
    delete(tasks.getByName("cacheReflections").outputs.files)
}

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

// Instructions for packaging a jar file - is a manifest even needed for modules?
tasks.named("jar") {
    // Make sure the assets directory is included
    dependsOn("cacheReflections")
    dependsOn("syncAssets")
    dependsOn("syncOverrides")
    dependsOn("syncDeltas")

    // Jarring needs to copy module.txt and all the assets into the output
    doFirst {
        copy {
            from("module.txt")
            into(mainSourceSet.output.classesDirs.first())
        }
    }

    finalizedBy("cleanReflections")
}

// Prep an IntelliJ module for the Terasology module - yes, might want to read that twice :D
configure<IdeaModel> {
    module {
        // Change around the output a bit
        inheritOutputDirs = false
        outputDir = buildDir.resolve("classes")
        testOutputDir = buildDir.resolve("testClasses")
        isDownloadSources = true
    }
}

// For Eclipse just make sure the classpath is right
configure<EclipseModel> {
    classpath {
        defaultOutputDir = file("build/classes")
    }
}
