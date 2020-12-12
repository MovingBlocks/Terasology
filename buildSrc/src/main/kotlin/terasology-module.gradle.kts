// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

// Simple build file for modules - the one under the Core module is the template, will be copied as needed to modules

import org.gradle.plugins.ide.eclipse.model.EclipseModel
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import org.terasology.module.ModuleMetadataJsonAdapter
import org.terasology.naming.Version

plugins {
    id("java")
    id("idea")
    id("eclipse")
}

// Read environment variables, including variables passed by jenkins continuous integration server
val env: MutableMap<String, String> by extra { System.getenv( )}
// This is a fun one ... when versions switched to dynamic -SNAPSHOT or not based on branch existing modules using `master` would suddenly try publishing releases
// This won't work without additionally doing constant version bumps (perhaps via Git tags) - but too much work to switch around all modules at once
// Complicating things more the use of publish.gradle to centralize logic means modules and engine bits are treated the same, yet we need to vary modules
// Temporary workaround: default modules to bypass release management: master branch builds will still make snapshot builds for the snapshot repo
// If a module actually wants release management simply include `"isReleaseManaged" : true` in module.txt - this is needed for the engine repo embedded modules
// One option would be to slowly convert modulespace to default to a `develop` + `master` setup living in harmony with associated automation/github tweaks
// Alternatively one more round of refactoring could switch to Git tags, a single `master` branch, and possible other things to help match snaps/PR builds somehow?
extra["bypassModuleReleaseManagement"] = true

val moduleFile = file("module.txt")

// The module file should always exist if the module was correctly created or cloned using Gradle
if (!moduleFile.exists()) {
    println("Y U NO EXIST MODULE.TXT!")
    throw GradleException("Failed to find module.txt for " + project.name)
}

class ModuleInfoException(
    cause: Throwable,
    @Suppress("MemberVisibilityCanBePrivate") val file: File? = null,
    private val project: Project? = null
) : RuntimeException(cause) {
    override val message: String
        get() {
            // trying to get the fully-qualified-class-name-mess off the front and just show
            // the useful part.
            val detail = cause?.cause?.localizedMessage ?: cause?.localizedMessage
            return "Error while reading module info from ${describeFile()}:\n  ${detail}"
        }

    private fun describeFile(): String {
        return if (project != null && file != null) {
            project.rootProject.relativePath(file)
        } else if (file != null) {
            file.toString()
        } else {
            "[unnamed file]"
        }
    }

    override fun toString(): String {
        val causeType = cause?.let { it::class.simpleName }
        return "ModuleInfoException(file=${describeFile()}, cause=${causeType})"
    }
}

val moduleConfig = try {
    moduleFile.reader().use {
        ModuleMetadataJsonAdapter().read(it)!!
    }
} catch (e: Exception) {
    throw ModuleInfoException(e, moduleFile, project)
}

// Check for an outright -SNAPSHOT in the loaded version - for ease of use we want to get rid of that everywhere, so warn about it and remove for the variable
if (moduleConfig.version.isSnapshot) {
    logger.warn("Module ${project.name} is explicitly versioned as a snapshot in module.txt, please remove '-SNAPSHOT'")

    moduleConfig.version = with(moduleConfig.version) {
        Version(major, minor, patch, false)
    }
}

// The only case in which we make module non-snapshots is if release management is enabled and BRANCH_NAME is "master"
val isReleaseManaged = moduleConfig.getExtension("isReleaseManaged", Boolean::class.java) ?: false
if (isReleaseManaged && env["BRANCH_NAME"] == "master") {
    // This is mildly awkward since we need to bypass by default, yet if release management is on (true) then we set the bypass to false ..
    extra["bypassModuleReleaseManagement"] = false
} else {
    // In the case where we actually are building a snapshot we load -SNAPSHOT into the version variable, even if it wasn't there in module.txt
    moduleConfig.version = moduleConfig.version.snapshot
}

project.version = moduleConfig.version
// Jenkins-Artifactory integration catches on to this as part of the Maven-type descriptor
project.group = "org.terasology.modules"

logger.info("Version for {} loaded as {} for group {}", project.name, project.version, project.group)

// Grab all the common stuff like plugins to use, artifact repositories, code analysis config, Artifactory settings, Git magic
// Note that this statement is down a ways because it is affected by the stuff higher in this file like setting versioning
apply(from = "$rootDir/config/gradle/publish.gradle")

// Handle some logic related to where what is
configure<SourceSetContainer> {
    named("main") {
        java.outputDir = File("$buildDir/classes")
    }
    named("test") {
        java.outputDir = File("$buildDir/testClasses")
    }
}
val convention = project.getConvention().getPlugin(JavaPluginConvention::class)
val mainSourceSet = convention.getSourceSets().getByName("main")

// TODO: Remove when we don't need to rely on snapshots. Needed here for solo builds in Jenkins
configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}


val deps = moduleConfig.dependencies.filterNotNull()
val moduleDepends = deps.filterNot { it.id.toString() == "engine" }
val engineVersion = deps.find { it.id.toString() == "engine" }?.versionRange()?.toString() ?: "+"

// Set dependencies. Note that the dependency information from module.txt is used for other Terasology modules
dependencies {
    // Check to see if this module is not the root Gradle project - if so we are in a multi-project workspace
    implementation(group = "org.terasology.engine", name = "engine", version = engineVersion) { isChanging = true }
    implementation(group = "org.terasology.engine", name = "engine-tests", version = engineVersion) { isChanging = true }

    for (gestaltDep in moduleDepends) {
        if (!gestaltDep.minVersion.isSnapshot) {
            // gestalt considers snapshots to satisfy a minimum requirement:
            // https://github.com/MovingBlocks/gestalt/blob/fe1893821127/gestalt-module/src/main/java/org/terasology/naming/VersionRange.java#L58-L59
            gestaltDep.minVersion = gestaltDep.minVersion.snapshot
            // (maybe there's some way to do that with a custom gradle resolver?
            // but making a resolver that only works that way on gestalt modules specifically
            // sounds complicated.)
        }

        val gradleDep = create(
            group = "org.terasology.modules",
            name = gestaltDep.id.toString(),
            version = gestaltDep.versionRange().toString()
        )

        if (gestaltDep.isOptional) {
            // `optional` module dependencies are ones it does not require for runtime
            // (but will use opportunistically if available)
            compileOnly(gradleDep) { isChanging = true }
            // though modules also sometimes use "optional" to describe their test dependencies;
            // they're not required for runtime, but they *are* required for tests.
            testImplementation(gradleDep) { isChanging = true }
        } else {
            implementation(gradleDep) { isChanging = true }
        }
    }

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.2")
    testImplementation("org.mockito:mockito-junit-jupiter:3.2.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")

    //backwards compatibility with modules tests
    testImplementation("junit:junit:4.12")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.5.2")
}


if (project.name == "ModuleTestingEnvironment") {
    dependencies {
        // MTE is a special snowflake, it gets these things as non-test dependencies
        implementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
        implementation("org.mockito:mockito-junit-jupiter:3.2.0")
        implementation("junit:junit:4.12")
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
        outputDir = file("build/classes")
        testOutputDir = file("build/testClasses")
        isDownloadSources = true
    }
}

// For Eclipse just make sure the classpath is right
configure<EclipseModel> {
    classpath {
        defaultOutputDir = file("build/classes")
    }
}
