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

        maven {
            // required to provide runtime dependencies to build-logic.
            name = "Terasology Artifactory"
            url = uri("https://artifactory.terasology.io/artifactory/virtual-repo-live")
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
val dirNatives by extra("natives")
val dirConfigMetrics by extra("config/metrics")
val templatesDir by extra(file("templates"))
// Lib dir for use in manifest entries etc (like in :engine). A separate "libsDir" exists, auto-created by Gradle
val subDirLibs by extra("libs")
val LwjglVersion by extra("3.3.3")

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Natives - Handles pulling in and extracting native libraries for LWJGL                                            //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// Define configurations for natives and config
val natives = configurations.create("natives")
val codeMetrics = configurations.create("codeMetrics")

dependencies {
    // For the "natives" configuration make it depend on the native files from LWJGL
    natives(platform("org.lwjgl:lwjgl-bom:$LwjglVersion"))
    listOf("natives-linux", "natives-windows", "natives-macos", "natives-macos-arm64").forEach {
        natives("org.lwjgl:lwjgl::$it")
        natives("org.lwjgl:lwjgl-assimp::$it")
        natives("org.lwjgl:lwjgl-glfw::$it")
        natives("org.lwjgl:lwjgl-openal::$it")
        natives("org.lwjgl:lwjgl-opengl::$it")
        natives("org.lwjgl:lwjgl-stb::$it")
    }


    // Config for our code analytics lives in a centralized repo: https://github.com/MovingBlocks/TeraConfig
    codeMetrics(group = "org.terasology.config", name = "codemetrics", version = "2.2.0", ext = "zip")

    // Natives for JNLua (Kallisti, KComputers)
    natives(group = "org.terasology.jnlua", name = "jnlua_natives", version = "0.1.0-SNAPSHOT", ext = "zip")

    // Natives for JNBullet
    natives(group = "org.terasology.jnbullet", name = "JNBullet", version = "1.0.2", ext = "zip")

}

tasks.register<Copy>("extractWindowsNatives") {
    description = "Extracts the Windows natives from the downloaded zip"
    from(configurations["natives"].filter { it.name.contains("natives-windows") }.map { zipTree(it) })
    into("$dirNatives/windows")
    exclude("META-INF/**")
}

tasks.register<Copy>("extractMacOSXNatives") {
    description = "Extracts the OSX natives from the downloaded zip"
    from(configurations["natives"].filter { it.name.contains("natives-macos") }.map { zipTree(it) })
    into("$dirNatives/macosx")
    exclude("META-INF/**")
}

tasks.register<Copy>("extractLinuxNatives") {
    description = "Extracts the Linux natives from the downloaded zip"
    from(configurations["natives"].filter { it.name.contains("natives-linux") }.map { zipTree(it) })
    into("$dirNatives/linux")
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
        "extractWindowsNatives",
        "extractLinuxNatives",
        "extractMacOSXNatives",
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
project(":modules").subprojects.forEach { proj ->
    project(":modules").subprojects {
        configurations.all {
            resolutionStrategy.dependencySubstitution {
                substitute(module("org.terasology.modules:${proj.name}")).using(project(":modules:${proj.name}"))
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
