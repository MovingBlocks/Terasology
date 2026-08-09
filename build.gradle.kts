// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import org.jetbrains.gradle.ext.ActionDelegationConfig
import org.jetbrains.gradle.ext.delegateActions
import org.jetbrains.gradle.ext.settings
import org.terasology.gradology.CopyButNeverOverwrite

// Dependencies needed for what our Gradle scripts themselves use. It cannot be included via an external Gradle file :-(
buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()

        // required to provide runtime dependencies to build-logic.
        maven {
            val repoViaEnv = System.getenv()["RESOLUTION_REPO"]
            if (rootProject.hasProperty("alternativeResolutionRepo")) {
                // If the user supplies an alternative repo via gradle.properties then use that
                name = "from alternativeResolutionRepo property"
                // Fun Gradle/Kotlin-ism: a general import at the top of a class used in a buildscript block won't help
                url =  java.net.URI(rootProject.property("alternativeResolutionRepo") as String)
            } else if (repoViaEnv != null && repoViaEnv != "") {
                name = "from \$RESOLUTION_REPO"
                url = java.net.URI(repoViaEnv)
            } else {
                // Our default is the main virtual repo containing everything except repos for testing Artifactory itself
                name = "Terasology Artifactory"
                url = java.net.URI("https://artifactory.terasology.io/artifactory/virtual-repo-live")
            }
        }

        // TODO MYSTERY: As of November 7th 2011 virtual-repo-live could no longer be relied on for latest snapshots - Pro feature?
        // We've been using it that way for *years* and nothing likewise changed in the area for years as well. This seems to work ....
        maven {
            name = "Terasology snapshot locals"
            url = uri("https://artifactory.terasology.io/artifactory/terasology-snapshot-local")
        }
    }

    dependencies {
        // Our locally included /build-logic
        classpath("org.terasology.gradology:build-logic")
    }
}

plugins {
    // Needed for extending the "clean" task to also delete custom stuff defined here like natives
    id("base")

    // needs for native platform("org.lwjgl") handling.
    id("java-platform")

    // The root project should not be an eclipse project. It keeps eclipse (4.2) from finding the sub-projects.
    //apply plugin: "eclipse"
    id("idea")
    // For the "Build and run using: Intellij IDEA | Gradle" switch
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"

    id("com.google.protobuf") version "0.9.4" apply false
    id("terasology-repositories")
}

// Test for right version of Java in use for running this script
assert(org.gradle.api.JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17))
if (JavaVersion.current() != JavaVersion.VERSION_17) {
    logger.warn("""
WARNING:
Compiling with a JDK of not version 17. If you encounter oddities try Java 17.
Current detected Java version is ${JavaVersion.current()}
 from vendor ${System.getProperty("java.vendor")}
located at ${System.getProperty("java.home")}
""")
}

// Declare "extra properties" (variables) for the project (and subs) - a Gradle thing that makes them special.
val dirNatives = "natives"
val dirConfigMetrics = "config/metrics"
val templatesDir = file("templates")
// Lib dir for use in manifest entries etc (like in :engine). A separate "libsDir" exists, auto-created by Gradle
val subDirLibs = "libs"
val LwjglVersion = "3.3.3"
// Published to extra: read via rootProject.extra[...] from other projects' build scripts
extra["dirNatives"] = dirNatives
extra["LwjglVersion"] = LwjglVersion

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Natives - Handles pulling in and extracting native libraries for LWJGL                                            //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// Define configurations for natives and config
val natives = configurations.create("natives")
val codeMetrics = configurations.create("codeMetrics")

dependencies {
    // For the "natives" configuration make it depend on the native files from LWJGL
    natives(platform("org.lwjgl:lwjgl-bom:$LwjglVersion"))
    listOf("natives-linux", "natives-linux-arm64", "natives-windows", "natives-windows-arm64", "natives-macos", "natives-macos-arm64").forEach {
        natives("org.lwjgl:lwjgl::$it")
        natives("org.lwjgl:lwjgl-assimp::$it")
        natives("org.lwjgl:lwjgl-glfw::$it")
        natives("org.lwjgl:lwjgl-openal::$it")
        natives("org.lwjgl:lwjgl-opengl::$it")
        natives("org.lwjgl:lwjgl-stb::$it")
    }


    // Config for our code analytics lives in a centralized repo: https://github.com/MovingBlocks/TeraConfig
    codeMetrics("org.terasology.config:codemetrics:2.2.0@zip")

    // Natives for JNLua (Kallisti, KComputers)
    natives("org.terasology.jnlua:jnlua_natives:0.1.0-SNAPSHOT@zip")

    // Natives for JNBullet
    // 1.0.5-SNAPSHOT until a proper release is tagged - see MovingBlocks/JNBullet#23, which added
    // the linux_windows_arm64_llvm_mingw32 (and linux_aarch64) native build targets we need here.
    natives("org.terasology.jnbullet:JNBullet:1.0.5-SNAPSHOT@zip")

}

// "natives-windows" is a substring of "natives-windows-arm64" (same for linux and macos), so a
// plain .contains() filter pulled both classifiers into one directory and one architecture's
// files silently overwrote the other's. Every directory below is suffixed with its architecture -
// amd64/arm64, matching what System.getProperty("os.arch") actually reports.
tasks.register<Copy>("extractWindowsAmd64Natives") {
    description = "Extracts the Windows amd64 natives from the downloaded zip"
    from(configurations["natives"].filter {
        it.name.contains("natives-windows") && !it.name.contains("natives-windows-arm64")
    }.map { zipTree(it) })
    into("$dirNatives/windows-amd64")
    exclude("META-INF/**")
}

tasks.register<Copy>("extractWindowsArm64Natives") {
    description = "Extracts the Windows arm64 natives from the downloaded zip"
    from(configurations["natives"].filter { it.name.contains("natives-windows-arm64") }.map { zipTree(it) })
    into("$dirNatives/windows-arm64")
    exclude("META-INF/**")
}

tasks.register<Copy>("extractMacOSAmd64Natives") {
    description = "Extracts the macOS amd64 natives from the downloaded zip"
    from(configurations["natives"].filter {
        it.name.contains("natives-macos") && !it.name.contains("natives-macos-arm64")
    }.map { zipTree(it) })
    into("$dirNatives/macos-amd64")
    exclude("META-INF/**")
}

tasks.register<Copy>("extractMacOSArm64Natives") {
    description = "Extracts the macOS arm64 natives from the downloaded zip"
    from(configurations["natives"].filter { it.name.contains("natives-macos-arm64") }.map { zipTree(it) })
    into("$dirNatives/macos-arm64")
    exclude("META-INF/**")
}

tasks.register<Copy>("extractLinuxAmd64Natives") {
    description = "Extracts the Linux amd64 natives from the downloaded zip"
    from(configurations["natives"].filter {
        it.name.contains("natives-linux") && !it.name.contains("natives-linux-arm64")
    }.map { zipTree(it) })
    into("$dirNatives/linux-amd64")
    exclude("META-INF/**")
}

tasks.register<Copy>("extractLinuxArm64Natives") {
    description = "Extracts the Linux arm64 natives from the downloaded zip"
    from(configurations["natives"].filter { it.name.contains("natives-linux-arm64") }.map { zipTree(it) })
    into("$dirNatives/linux-arm64")
    exclude("META-INF/**")
}

tasks.register<Copy>("extractJNLuaNatives") {
    description = "Extracts the JNLua natives from the downloaded zip"
    from(configurations["natives"].filter { it.name.contains("jnlua") }.map { zipTree(it) })
    into("$dirNatives")
}

tasks.register<Copy>("extractNativeBulletNatives") {
    description = "Extracts the JNBullet natives from the downloaded zip"
    from(configurations["natives"].filter { it.name.contains("JNBullet") }.map { zipTree(it) })
    into("$dirNatives")
}

tasks.register("extractNatives") {
    description = "Extracts all the native lwjgl libraries from the downloaded zip"
    dependsOn(
        "extractWindowsAmd64Natives",
        "extractWindowsArm64Natives",
        "extractLinuxAmd64Natives",
        "extractLinuxArm64Natives",
        "extractMacOSAmd64Natives",
        "extractMacOSArm64Natives",
        "extractJNLuaNatives",
        "extractNativeBulletNatives"
    )
    // specifying the outputs directory lets gradle have an up-to-date check, and automatic clean task
    outputs.dir("$dirNatives")
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Helper tasks                                                                                                      //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

tasks.register<Copy>("extractConfig") {
    description = "Extracts our configuration files from the zip we fetched as a dependency"
    from(configurations["codeMetrics"].map { zipTree(it) })
    into("$rootDir/$dirConfigMetrics")
}

tasks.named("clean") {
    // gradle autocreates a clean task for tasks if outputs is specified, just link them to general clean.
    dependsOn("cleanExtractConfig", "cleanExtractNatives")
    println("Cleaned root - don't forget to re-extract stuff! 'gradlew extractNatives extractConfig' will do so")
}

// Magic for replace remote dependency on local project (source)
// for Engine
allprojects {
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("org.terasology.engine:engine")).using(project(":engine")).because("we have sources!")
            substitute(module("org.terasology.engine:engine-tests")).using(project(":engine-tests"))
                .because("we have sources!")
        }
    }
}

// Magic for replace remote dependency on local project (source)
// For exists modules
//
// Applies to :modules itself as well as its subprojects - the substitution rule was previously
// only being configured on the subprojects' own configurations, never on :modules's own
// "classpath" configuration (the one :modules:fetchModuleDependencies actually resolves to
// populate cachedModules/). That gap meant every module with a local source checkout still got
// its external artifact pulled into cachedModules/ right alongside the local build, and gestalt's
// module scanner (which doesn't know about Gradle's substitution rules at all) would discover
// both at runtime - keeping whichever it happened to scan first, non-deterministically.
val moduleProjectNames = project(":modules").subprojects.map { it.name }
(project(":modules").subprojects + project(":modules")).forEach { targetProject ->
    targetProject.configurations.all {
        resolutionStrategy.dependencySubstitution {
            moduleProjectNames.forEach { moduleName ->
                substitute(module("org.terasology.modules:$moduleName")).using(project(":modules:$moduleName"))
                    .because("we have sources!")
            }
        }
    }
}

tasks.withType<Wrapper> {
    // ALL distributionType because IntelliJ prefers having its sources for analysis and reference.
    distributionType = Wrapper.DistributionType.ALL
}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// General IDE customization                                                                                         //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

tasks.register<CopyButNeverOverwrite>("copyInMissingTemplates") {
    description = "Copies in placeholders from the /templates dir to project root if not present yet"
    from(templatesDir)
    into(rootDir)
    include("gradle.properties", "override.cfg")
}

tasks.register<CopyButNeverOverwrite>("jmxPassword") {
    description = "Create config/jmxremote.password from a template."

    filePermissions { unix("600") } // passwords must be accessible only by owner

    // there is a template file in $JAVA_HOME/conf/management
    from(java.nio.file.Path.of(System.getProperty("java.home"), "conf", "management"))
    include("jmxremote.password.template")
    rename("(.*).template", "$1")
    into("config")

    doLast {
        logger.warn("${this.outputs.files.singleFile}/jmxremote.password:100: Edit this to set your password.")
    }
}

// Make sure the IDE prep includes extraction of natives
tasks.named("ideaModule") {
    dependsOn("extractNatives", "copyInMissingTemplates")
}

// For IntelliJ add a bunch of excluded directories
idea {
    module {
        excludeDirs = setOf(
            // Exclude Eclipse dirs
            // TODO: Update this as Eclipse bin dirs now generate in several deeper spots rather than at top-level
            file("bin"),
            file(".settings"),
            // TODO: Add a single file exclude for facades/PC/Terasology.launch ?

            // Exclude special dirs
            file("natives"),
            file("protobuf"),

            // Exclude output dirs
            file("configs"),
            file("logs"),
            file("saves"),
            file("screenshots"),
            file("terasology-server"),
            file("terasology-2ndclient")
        )
        isDownloadSources = true
    }

    project.settings.delegateActions {
        delegateBuildRunToGradle = false
        testRunner = ActionDelegationConfig.TestRunner.CHOOSE_PER_TEST
    }
}

tasks.register("cleanIdeaIws") {
    doLast {
        File("Terasology.iws").delete()
    }
}

tasks.named("cleanIdea") {
    dependsOn("cleanIdeaIws")
}

// A task to assemble various files into a single zip for distribution as "build-harness.zip" for module builds
tasks.register<Zip>("assembleBuildHarness") {
    description = "Assembles a zip of files useful for module development"

    dependsOn("extractNatives")
    from("natives") {
        include("**/*")
        // TODO: use output of extractNatives?
        // TODO: which module needs natives to build?
        into("natives")
    }

    dependsOn("extractConfig")
    from("config") {
        //include "gradle/**/*", "metrics/**/*"
        include("**/*")
        // TODO: depend on output of extractConfig?
        into("config")
    }

    from("gradle") {
        include("**/*") // include all files in "gradle"
        // TODO: exclude groovy jar?
        into("gradle")
    }

    from("build-logic") {
        include("src/**", "*.kts")
        into("build-logic")
    }

    from("templates") {
        include("build.gradle")
    }

    from(".") {
        include("gradlew")
    }

    // include file "templates/module.logback-test.xml" as "src/test/resources/logback-test.xml"
    from("templates") {
        include("module.logback-test.xml")
        rename("module.logback-test.xml", "logback-test.xml")
        into("src/test/resources")
    }

    // set the archive name
    archiveFileName.set("build-harness.zip")
}
