// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

// The PC facade is responsible for the primary distribution - a plain Java application runnable on PCs

import org.apache.tools.ant.filters.FixCrLfFilter
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.plugins.ide.idea.model.IdeaModel
import java.text.SimpleDateFormat
import java.util.*

plugins {
    application
}

// Grab all the common stuff like plugins to use, artifact repositories, code analysis config
apply(from = "$rootDir/config/gradle/publish.gradle")

val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
dateTimeFormat.timeZone = TimeZone.getTimeZone("UTC")


/**
 * The subdirectory for this development environment.
 *
 * Only use this to run local processes. When building releases, you will be targeting other
 * operating systems in addition to your own.
 *
 * @return
 */
fun nativeSubdirectoryName(): String {
    return when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> "windows"
        Os.isFamily(Os.FAMILY_MAC) -> "macosx"
        Os.isFamily(Os.FAMILY_UNIX) -> "linux"
        else -> {
            logger.warn("What kind of libraries do you use on this? {}", System.getProperty("os.name"))
            "UNKNOWN"
        }
    }
}

fun isMacOS() : Boolean {
    return Os.isFamily(Os.FAMILY_MAC)
}


// Default path to store server data if running headless via Gradle
val localServerDataPath by extra("terasology-server")

// General props
val mainClassName by extra("org.terasology.engine.Terasology")
val subDirLibs = "libs"
val templatesDir = File(rootDir, "templates")
val rootDirDist = File(rootDir, "build/distributions")

// Inherited props
val dirNatives: String by rootProject.extra
val distsDirectory: DirectoryProperty by project

// Read environment variables, including variables passed by jenkins continuous integration server
val env: MutableMap<String, String> = System.getenv()!!

// Version related
val startDateTimeString = dateTimeFormat.format(Date())!!
val versionFileName = "VERSION"
val versionBase by lazy { File(templatesDir, "version.txt").readText().trim() }
val displayVersion = versionBase


application {
    mainClass.set(extra.get("mainClassName") as String)
}

// Adjust as the Gradle 6 upgrade changed this path a bit
sourceSets {
    main { java.outputDir = File("$buildDir/classes") }
    test { java.outputDir = File("$buildDir/testClasses") }
}

// Base the engine tests on the same version number as the engine
version = project(":engine").version
logger.info("PC VERSION: {}", version)

// Jenkins-Artifactory integration catches on to this as part of the Maven-type descriptor
group = "org.terasology.facades"

dependencies {
    implementation(project(":engine"))
    implementation(group = "org.reflections", name = "reflections", version = "0.9.10")
    implementation(project(":subsystems:DiscordRPC"))

    // TODO: Consider whether we can move the CR dependency back here from the engine, where it is referenced from the main menu
    implementation(group = "org.terasology.crashreporter", name = "cr-terasology", version = "4.1.0")

    // Make sure any local module builds are up-to-date and have their dependencies by declaring
    // a runtime dependency on whatever the `:modules` subproject declares.
    // This won't add anything if there are no modules checked out.
    runtimeOnly(platform(project(":modules")))
}

// Instructions for packaging a jar file for the PC facade
tasks.named<Jar>("jar") {
    manifest {
        //TODO: Maybe later add the engine's version number into here?
        attributes["Main-Class"] = mainClassName
        attributes["Class-Path"] = configurations["runtimeClasspath"].map { it.name }.joinToString(" ")
        attributes["Implementation-Title"] = "Terasology-" + project.name
        attributes["Implementation-Version"] = """${env["BUILD_NUMBER"]}, ${env["GIT_BRANCH"]}, ${env["BUILD_ID"]}"""
    }
}

configurations {
    register("modules") {
        description = "for fetching modules for running a server"
        isTransitive = false
    }
}

// Used for all game configs.
fun JavaExec.commonConfigure() {
    group = "terasology run"

    dependsOn(":extractNatives")
    dependsOn("classes")

    // Run arguments
    main = mainClassName
    workingDir = rootDir

    classpath(sourceSets["main"].runtimeClasspath)

    args("-homedir")
    jvmArgs("-Xmx3072m")

    if (isMacOS()) {
        args("-noSplash")
        jvmArgs("-XstartOnFirstThread", "-Djava.awt.headless=true")
    }
}

tasks.register<JavaExec>("game") {
    commonConfigure()
    description = "Run 'Terasology' to play the game as a standard PC application"

    // If there are no actual source modules let the user know, just in case ..
    if (project(":modules").subprojects.isEmpty()) {
        logger.warn("NOTE: You're running the game from source without any source modules - that may be intentional (got jar modules?) but maybe not. Consider running `groovyw init` or a variant (see `groovyw usage`)")
    }
}

tasks.register<JavaExec>("profile") {
    commonConfigure()
    description = "Run 'Terasology' to play the game as a standard PC application (with Java FlightRecorder profiling)"
    jvmArgs( "-XX:+UnlockCommercialFeatures", "-XX:+FlightRecorder", "-XX:+UnlockDiagnosticVMOptions", "-XX:+DebugNonSafepoints", "-XX:StartFlightRecording=filename=terasology.jfr,dumponexit=true")
}

tasks.register<JavaExec>("debug") {
    commonConfigure()
    description = "Run 'Terasology' to play the game as a standard PC application (in debug mode)"
    jvmArgs( "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044")
}

tasks.register<JavaExec>("permissiveNatives") {
    commonConfigure()
    description = "Run 'Terasology' with security set to permissive and natives loading a second way (for KComputers)"

    args("-permissiveSecurity")
    systemProperty("java.library.path", rootProject.file(dirNatives + "/" + nativeSubdirectoryName()))
}

apply(from="server.build.gradle")

// TODO: Seems to always be up to date so no modules get copied
tasks.register<Sync>("setupServerModules") {
    description =
        """Parses "extraModules" - a comma-separated list of modules and puts them into $localServerDataPath"""

    val extraModules: String? by project
    extraModules?.let {
        // Grab modules from Artifactory - cheats by declaring them as dependencies
        it.splitToSequence(",").forEach {
            logger.info("Extra module: {}", it)
            dependencies {
                "modules"(group = "org.terasology.modules", name = it, version = "+")
            }
        }
    }

    from(configurations.named("modules"))
    into(File(rootProject.file(localServerDataPath), "modules"))
}

// TODO: Make a task to reset server / game data
tasks.register<JavaExec>("server") {
    commonConfigure()
    description = "Starts a headless multiplayer server with data stored in [project-root]/$localServerDataPath"
    dependsOn("setupServerConfig")
    dependsOn("setupServerModules")
    args("-headless", "-homedir=$localServerDataPath")
}

// Preps a version file to bundle with PC dists. This eventually goes into the root of a zip file
tasks.register<Copy>("createVersionFile") {
    inputs.property("dateTime", startDateTimeString)
    onlyIf { env["BUILD_URL"] != null }
    from(templatesDir)
    into("$buildDir")
    include(versionFileName)
    expand(mapOf(
        "buildNumber" to env["BUILD_NUMBER"],
        "buildUrl" to env["BUILD_URL"],
        "gitBranch" to env["GIT_BRANCH"],
        "dateTime" to startDateTimeString,
        "displayVersion" to displayVersion
    ))
    filter(FixCrLfFilter::class, "eol" to FixCrLfFilter.CrLf.newInstance("crlf"))
}

// TODO: This could probably be done more Gradley (engine project resource dir instead of direct path?) and with some variables
tasks.register<Copy>("copyCreditsFile") {
    description = "Copies the credits file into the engine's resource dir where it'll be read at runtime"
    from("$rootDir/docs")
    into("$rootDir/engine/src/main/resources")
    include("Credits.md")
}

// Main application dist target. Does NOT include any modules.
tasks.register<Sync>("distApp") {
    description = "Creates an application package for distribution"
    group = "terasology dist"

    dependsOn("createVersionFile")
    dependsOn("copyCreditsFile")
    dependsOn(":extractNatives")
    dependsOn("jar")

    into("${distsDirectory.get().asFile}/app")
    from ("$rootDir/README.markdown") {
        filter(FixCrLfFilter::class, "eol" to FixCrLfFilter.CrLf.newInstance("crlf"))
        rename("README.markdown", "README")
    }
    from ("$rootDir/LICENSE") {
        filter(FixCrLfFilter::class, "eol" to FixCrLfFilter.CrLf.newInstance("crlf"))
    }
    from ("$rootDir/NOTICE") {
        filter(FixCrLfFilter::class, "eol" to FixCrLfFilter.CrLf.newInstance("crlf"))
    }
    from("launchScripts") {
        exclude("TeraEd.exe")
    }

    from("$buildDir/$versionFileName") {}

    into(subDirLibs) {
        from(configurations.runtimeClasspath)
        from(tasks.getByPath(":engine:jar"))
        from("$buildDir/libs") {
            include("*.jar")
            rename {
                "Terasology.jar"
            }
        }
    }
    into(dirNatives) {
        from("$rootDir/$dirNatives")
    }
}

// Distribute modules - only grabs Core in Jenkins but locally will grab any present. "Distros" now handle Jenkins packs
tasks.register<Sync>("distModules") {
    description = "Prepares local modules for distribution"
    dependsOn("distApp")
    dependsOn(":moduleJars")

    // So this is probably a hack, but it works ;-) It does not work if it is in distApp, default "into" quirk ?
    into("${distsDirectory.get().asFile}/app/modules")
    // FIXME: duplicating code from /build.gradle:terasologyModules
    val terasologyModules = rootProject.subprojects.filter { it.parent?.name == "modules" }
    terasologyModules.forEach {
        from("$rootDir/modules/${it.name}/build/libs")
        include("*.jar")
    }
}

tasks.register<Zip>("distPCZip") {
    group = "terasology dist"
    dependsOn("distApp")
    dependsOn("distModules")
    from("${distsDirectory.get().asFile}/app")
    archiveFileName.set("Terasology.zip")
}

tasks.register<Sync>("distForLauncher") {
    group = "terasology dist"

    into(rootDirDist)
    from(tasks.getByName("distPCZip"))

    into("../resources/main/org/terasology/version") {
        from("$rootDir/engine/build/classes/org/terasology/version") {
            include("versionInfo.properties")
        }
    }
}

// Prep an IntelliJ module for the facade
configure<IdeaModel> {
    module {
        // Change around the output a bit
        inheritOutputDirs = false
        outputDir = file("build/classes")
        testOutputDir = file("build/testClasses")
    }
}

tasks.register<Copy>("copyEclipseLauncher") {
    from("$rootDir/config/eclipse")
    into(projectDir)
    include("Terasology.launch")
}

tasks.named("eclipse") {
    dependsOn("copyEclipseLauncher")
    dependsOn(":extractNatives")
}

tasks.named("cleanEclipse") {
    doLast {
        File(projectDir, "Terasology.launch").delete()
    }
}
