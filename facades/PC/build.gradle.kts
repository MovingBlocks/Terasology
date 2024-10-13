// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

// The PC facade is responsible for the primary distribution - a plain Java application runnable on PCs

import Terasology_dist_gradle.ValidateZipDistribution
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.apache.tools.ant.filters.FixCrLfFilter
import org.terasology.gradology.RunTerasology
import org.terasology.gradology.nativeSubdirectoryName
import kotlin.test.assertEquals
import kotlin.test.fail

plugins {
    application
    id("terasology-dist")
    id("facade")
}

// Grab all the common stuff like plugins to use, artifact repositories, code analysis config
apply(from = "$rootDir/config/gradle/publish.gradle")

// Default path to store server data if running headless via Gradle
val localServerDataPath by extra("terasology-server")

// General props
val mainClassName by extra("org.terasology.engine.Terasology")
val templatesDir = File(rootDir, "templates")
val rootDirDist = File(rootDir, "build/distributions")

// Inherited props
val dirNatives: String by rootProject.extra
val distsDirectory: DirectoryProperty by project

// Read environment variables, including variables passed by jenkins continuous integration server
val env: MutableMap<String, String> = System.getenv()!!

// Version related
val startDateTimeString = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
val versionFileName = "VERSION"
val versionBase by lazy { File(templatesDir, "version.txt").readText().trim() }
val displayVersion = versionBase


application {
    applicationName = "Terasology"
    executableDir = ""
    mainClass.set(extra.get("mainClassName") as String)
}

// Base the engine tests on the same version number as the engine
version = project(":engine").version
logger.info("PC VERSION: {}", version)

// Jenkins-Artifactory integration catches on to this as part of the Maven-type descriptor
group = "org.terasology.facades"

dependencies {
    implementation(libs.jna.platform)
    implementation(group = "info.picocli", name = "picocli", version = "4.5.2")
    annotationProcessor("info.picocli:picocli-codegen:4.5.2")

    implementation(project(":engine"))
    implementation(project(":subsystems:DiscordRPC"))
    implementation("io.projectreactor:reactor-core:3.4.7")

    // TODO: Consider whether we can move the CR dependency back here from the engine, where it is referenced from the main menu
    implementation(group = "org.terasology.crashreporter", name = "cr-terasology", version = "5.0.0")

    runtimeOnly(libs.logback) {
        because("to configure logging with logback.xml")
    }
    runtimeOnly("org.codehaus.janino:janino:3.1.7") {
        because("allows use of EvaluatorFilter in logback.xml")
    }
    runtimeOnly(libs.slf4j.jul) {
        because("redirects java.util.logging from miscellaneous dependencies through slf4j")
    }

    testImplementation(platform("org.junit:junit-bom:5.10.1")) {
        // junit-bom will set version numbers for the other org.junit dependencies.
    }
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)

    testImplementation("com.google.truth:truth:1.1.2")
    testImplementation("com.google.truth.extensions:truth-java8-extension:1.1.2")
}

tasks.named<JavaCompile>("compileJava") {
    // according to https://picocli.info/#_gradle_2
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

/****************************************
 * Run Targets
 */

tasks.register<RunTerasology>("game") {
    description = "Run 'Terasology' to play the game as a standard PC application"

    // If there are no actual source modules let the user know, just in case ..
    if (project(":modules").subprojects.isEmpty()) {
        logger.warn("NOTE: You're running the game from source without any source modules - that may be intentional (got jar modules?) but maybe not. Consider running `groovyw init` or a variant (see `groovyw usage`)")
    }
}

tasks.register<RunTerasology>("profile") {
    description = "Run 'Terasology' to play the game as a standard PC application (with Java FlightRecorder profiling)"
    jvmArgs( "-XX:+UnlockCommercialFeatures", "-XX:+FlightRecorder", "-XX:+UnlockDiagnosticVMOptions", "-XX:+DebugNonSafepoints", "-XX:StartFlightRecording=filename=terasology.jfr,dumponexit=true")
}

tasks.register<RunTerasology>("debug") {
    description = "Run 'Terasology' to play the game as a standard PC application (in debug mode)"
    jvmArgs( "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044")
}

tasks.register<RunTerasology>("permissiveNatives") {
    description = "Run 'Terasology' with security set to permissive and natives loading a second way (for KComputers)"

    args("--permissive-security")
    systemProperty("java.library.path", rootProject.file(dirNatives + "/" + nativeSubdirectoryName()))
}

// TODO: Make a task to reset server / game data
tasks.register<RunTerasology>("server") {
    description = "Starts a headless multiplayer server with data stored in [project-root]/$localServerDataPath"
    args = listOf("--headless", "--homedir=$localServerDataPath")
}


/*********************************
 * Distribution
 *
 * See also publish.gradle, included near the top.
 */

tasks.named<Jar>("jar") {
    // Launcher expects the main class to be in the file with this name.
    archiveFileName.set("Terasology.jar")

    manifest {
        //TODO: Maybe later add the engine's version number into here?
        attributes["Main-Class"] = mainClassName
        // A classpath in the manifest avoids the problem of having to put a classpath on the command line and
        // "line is too long" errors: https://github.com/gradle/gradle/issues/1989
        attributes["Class-Path"] = configurations["runtimeClasspath"].joinToString(" ") { it.name }
        attributes["Implementation-Title"] = "Terasology-" + project.name
        attributes["Implementation-Version"] =
            "$displayVersion, facade v${project.version}, build number ${env["BUILD_NUMBER"]}"
    }
}

/**
 * Create a human-readable file with version and build information.
 *
 * This goes in to the root of the distribution where it can easily be found and read by humans.
 * For build details in a easily parsed format, see the `versionInfo.properties` resource added
 * in engine's build.
 */
val createVersionFile = tasks.register<Copy>("createVersionFile") {
    this.description = "Create a human-readable file with version and build information."

    inputs.property("dateTime", startDateTimeString)
    from(templatesDir)
    into(layout.buildDirectory.dir("versionfile").get().asFile)
    include(versionFileName)
    expand(mapOf(
        "buildNumber" to env["BUILD_NUMBER"],
        "buildUrl" to env["BUILD_URL"],
        "dateTime" to startDateTimeString,
        "displayVersion" to displayVersion
    ))
    filter(FixCrLfFilter::class, "eol" to FixCrLfFilter.CrLf.newInstance("crlf"))
}

val distForLauncher = tasks.register<Zip>("distForLauncher") {
    group = "terasology dist"
    description = "Bundles the project to a Launcher-compatible layout."

    archiveFileName.set("Terasology.zip")

    // Launcher expects `libs/Terasology.jar`, no containing folder
    // TODO: fix launcher so it can take either structure. It should be able to do without ambiguity.
    val defaultLibraryDirectory = "lib"
    val launcherLibraryDirectory = "libs"

    this.with(distributions.getByName("main").contents {
        eachFile {
            val pathSegments = relativePath.segments

            when (pathSegments[0]) {
                defaultLibraryDirectory -> {
                    // Redirect things from lib/ to libs/
                    val tail = pathSegments.sliceArray(1 until pathSegments.size)
                    relativePath = RelativePath(true, launcherLibraryDirectory, *tail)
                }
            }

            if (this.sourcePath == "Terasology" || this.sourcePath == "Terasology.bat") {
                // I don't know how the "lib/" makes its way in to the classpath used by CreateStartScripts,
                // so we're adjusting it after-the-fact.
                filter(ScriptClasspathRewriter(this, defaultLibraryDirectory, launcherLibraryDirectory) as Transformer<String?, String>)
            }
        }
    })
}

tasks.register<ValidateZipDistribution>("testDistForLauncher") {
    description = "Validates locations in distForLauncher."

    fromTask(distForLauncher)

    doLast {
        val theFile = zipFile.get().asFile
        assertEquals("Terasology.zip", theFile.name)

        assertContainsPath("libs/Terasology.jar")
        assertContainsPath("/Terasology.bat")
    }
}

tasks.register<ValidateZipDistribution>("testDistZip") {
    description = "Validates locations in distZip."

    fromTask(tasks.named<Zip>("distZip"))

    doLast {
        assertContainsPath("*/lib/Terasology.jar")
        assertContainsPath("*/Terasology.bat")

        val rootFiles = tree.matching {
            include("/*")
        }
        if (!rootFiles.isEmpty) {
            fail("Expected a single root directory, but root contains files ${rootFiles.files.map { it.name }}")
        }
    }
}

tasks.register<Task>("testDist") {
    group = "verification"
    dependsOn("testDistForLauncher", "testDistZip")
}

class ScriptClasspathRewriter(file: FileCopyDetails, val oldDirectory: String, val newDirectory: String) : Transformer<String?, String> {
    private val isBatchFile = file.name.endsWith(".bat")

    override fun transform(line: String): String = if (isBatchFile) {
            line.replace("$oldDirectory\\", "$newDirectory\\")
        } else {
            line.replace("$oldDirectory/", "$newDirectory/")
        }
}

tasks.named<CreateStartScripts>("startScripts") {
    // Use start scripts that invoke java with `-jar` with the classpath in the jar manifest,
    // instead of including classpath on the command line. Avoids "line is too long" errors.
    // See https://github.com/gradle/gradle/issues/1989
    (unixStartScriptGenerator as TemplateBasedScriptGenerator).apply {
        template = resources.text.fromFile("src/main/startScripts/unixStartScript.gsp")
    }
    (windowsStartScriptGenerator as TemplateBasedScriptGenerator).apply {
        template = resources.text.fromFile("src/main/startScripts/windowsStartScript.bat.gsp")
    }
}

distributions {
    main {
        contents {
            from(rootDir) {
                include("README.markdown", "LICENSE", "NOTICE")
                rename("README.markdown", "README")
                filter(FixCrLfFilter::class, "eol" to FixCrLfFilter.CrLf.newInstance("crlf"))
            }
            from(createVersionFile)
            from(configurations.named("natives")) {
                into(dirNatives)
            }
        }
    }
}


/********************************
 * Eclipse Integration
 */

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
