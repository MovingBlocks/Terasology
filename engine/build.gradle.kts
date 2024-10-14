// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

// The engine build is the primary Java project and has the primary list of dependencies
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    id("java-library")
    id("org.jetbrains.gradle.plugin.idea-ext")
    id("com.google.protobuf")
    id("terasology-common")
}

// Grab all the common stuff like plugins to use, artifact repositories, code analysis config, etc
apply(from = "$rootDir/config/gradle/publish.gradle")

// Read environment variables, including variables passed by jenkins continuous integration server
val env = System.getenv()

// Stuff for our automatic version file setup
val startDateTimeString = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
val displayVersion by lazy { File("$rootDir/templates/version.txt").readText().trim() }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Java Section                                                                                                      //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

configure<SourceSetContainer> {
    // Adjust output path (changed with the Gradle 6 upgrade, this puts it back)
    main {
        java.destinationDirectory.set(layout.buildDirectory.dir("classes"))
        proto.srcDir("src/main/protobuf")
    }
    test { java.destinationDirectory.set(layout.buildDirectory.dir("testClasses")) }
}

// Customizations for the main compilation configuration
configurations {

    // Exclude a couple JWJGL modules that aren't needed during compilation (OS specific stuff in these two perhaps)
    implementation {
        exclude(module = "lwjgl-platform")
        exclude(module = "jinput-platform")
    }
}

configurations.configureEach {
    resolutionStrategy {
        // always pick reflections fork
        dependencySubstitution {
            substitute(module("org.reflections:reflections")).using(module("org.terasology:reflections:0.9.12-MB"))
        }
    }
}

// Primary dependencies definition
dependencies {
    // Storage and networking
    api(libs.guava)
    api(libs.gson)
    api("net.sf.trove4j:trove4j:3.0.3")
    implementation("io.netty:netty-all:4.1.77.Final")
    implementation("com.google.protobuf:protobuf-java:3.22.0")
    implementation("org.lz4:lz4-java:1.8.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    // Javax for protobuf due to @Generated - needed on Java 9 or newer Javas
    // TODO: Can likely replace with protobuf Gradle task and omit the generated source files instead
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    //Utilities
    api("org.codehaus.plexus:plexus-utils:3.0.16")

    // Java magic
    implementation("net.java.dev.jna:jna-platform:5.6.0")
    implementation("org.terasology:reflections:0.9.12-MB")
    implementation("com.esotericsoftware:reflectasm:1.11.9")

    // Graphics, 3D, UI, etc
    api(platform("org.lwjgl:lwjgl-bom:${rootProject.extra["LwjglVersion"]}"))
    api("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-assimp")
    api("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-openal")
    api("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-stb")

    implementation("io.micrometer:micrometer-core:1.9.12")
    implementation("io.micrometer:micrometer-registry-jmx:1.9.12")
    api("io.projectreactor:reactor-core:3.4.18")
    api("io.projectreactor.addons:reactor-extra:3.4.8")
    implementation("io.projectreactor.netty:reactor-netty-core:1.0.19")

    api("org.joml:joml:1.10.0")
    api("org.terasology.joml-ext:joml-geometry:0.1.0")

    implementation("org.abego.treelayout:org.abego.treelayout.core:1.0.3")
    api("com.miglayout:miglayout-core:5.0")
    implementation("de.matthiasmann.twl:PNGDecoder:1111")

    // Logging
    implementation(libs.slf4j.api) {
        because("a backend-independent Logger")
    }
    implementation(libs.logback) {
        because("telemetry implementation uses logback to send to logstash " +
                "and we bundle org.terasology.logback for the regex filter")
    }

    // audio
    implementation("com.projectdarkstar.ext.jorbis:jorbis:0.0.17")

    // Small-time 3rd party libs we"ve stored in our Artifactory for access
    implementation("ec.util:MersenneTwister:20")

    // telemetry
    implementation("com.snowplowanalytics:snowplow-java-tracker:0.12.1") {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // JSemVer (Semantic Versioning) - A dependency of Gestalt
    implementation("com.github.zafarkhaja:java-semver:0.10.2")

    // Our developed libs
    api(libs.gestalt.core)
    api(libs.gestalt.module)
    api(libs.gestalt.entitysystem)
    api(libs.gestalt.util)
    api(libs.gestalt.inject)

    annotationProcessor(libs.gestalt.injectjava)

    api("org.terasology:TeraMath:1.5.0")
    api("org.terasology:splash-screen:1.1.1")
    api("org.terasology.jnlua:JNLua:0.1.0-SNAPSHOT")
    api("org.terasology.jnbullet:JNBullet:1.0.4")
    api(libs.terasology.nui)
    api(libs.terasology.nuireflect)
    api(libs.terasology.nuigestalt)


    // Wildcard dependency to catch any libs provided with the project (remote repo preferred instead)
    api(fileTree("libs") { include("*.jar") })

    // TODO: Consider moving this back to the PC Facade instead of having the engine rely on it?
    implementation("org.terasology.crashreporter:cr-terasology:5.0.0")

    api(project(":subsystems:TypeHandlerLibrary"))

    implementation("io.github.benjaminamos.TracyJavaBindings:TracyJavaBindings:1.0.0-SNAPSHOT")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.22.0"
    }
    plugins {
    }
}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Version file stuff                                                                                                //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// First read the internal version out of the engine"s module.txt
val moduleFile = layout.projectDirectory.file("src/main/resources/org/terasology/engine/module.txt").asFile

println("Scanning for version in module.txt for engine")
val moduleConfig = groovy.json.JsonSlurper().parseText(moduleFile.readText()) as Map<String, String>

// Gradle uses the magic version variable when creating the jar name (unless explicitly set differently)
version = moduleConfig["version"]!!

// Jenkins-Artifactory integration catches on to this as part of the Maven-type descriptor
group = "org.terasology.engine"

println("Version for $project.name loaded as $version for group $group")

// This version info file actually goes inside the built jar and can be used at runtime, contents:
// displayVersion=alpha-20
// engineVersion=5.4.0-SNAPSHOT
tasks.register<WriteProperties>("createVersionInfoFile") {
    mapOf(
        "buildNumber" to env["BUILD_NUMBER"],
        "buildId" to env["BUILD_ID"],
        "buildTag" to env["BUILD_TAG"],
        "buildUrl" to env["BUILD_URL"],
        "jobName" to env["JOB_NAME"],
        "gitCommit" to env["GIT_COMMIT"],
        "displayVersion" to displayVersion,
        "engineVersion" to version
    ).filterValues { it != null }.forEach { (key, value) ->
        property(key, value!!)
        inputs.property(key, value)
    }
    if (env["JOB_NAME"] != null) {
        property("dateTime", startDateTimeString)
    }
    destinationFile = layout.buildDirectory.dir("classes/org/terasology/engine/version").get().file("versionInfo.properties")
}

tasks.named<Copy>("processResources") {
    from("$rootDir/docs") {
        include("Credits.md")
    }
}

//TODO: Remove this when gestalt can handle ProtectionDomain without classes (Resources)
tasks.register<Copy>("copyResourcesToClasses") {
    from("processResources")
    into(sourceSets["main"].output.classesDirs.first())
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(
        tasks.named("copyResourcesToClasses"),
        tasks.named("createVersionInfoFile")
    )
    // Create an asset list during compilation (needed for Gestalt 8)
    inputs.files(sourceSets.main.get().resources.srcDirs)
    options.compilerArgs = arrayListOf("-Aresource=${sourceSets.main.get().resources.srcDirs.joinToString(File.pathSeparator)}")
}
tasks.named<JavaCompile>("compileTestJava") {
    dependsOn(
        tasks.named("copyResourcesToClasses"),
        tasks.named("createVersionInfoFile")
    )
    // Create an asset list during compilation (needed for Gestalt 8)
    inputs.files(sourceSets.test.get().resources.srcDirs)
    options.compilerArgs = arrayListOf("-Aresource=${sourceSets.test.get().resources.srcDirs.joinToString(File.pathSeparator)}")
}

// Instructions for packaging a jar file for the engine
tasks.withType<Jar> {
    // Unlike the content modules Gradle grabs the assets as they're in a resources directory. Need to avoid dupes tho
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Class-Path"] = "libs/" + configurations.runtimeClasspath.get().joinToString(" libs/") { it.name }
        attributes["Implementation-Title"] = "Terasology"
        attributes["Implementation-Version"] =
            "$displayVersion, engine v${project.version}, build number ${env["BUILD_NUMBER"]}"
    }
}

// JMH related tasks

sourceSets {
    create("jmh") {
        java.srcDir("src/jmh/java")
        resources.srcDir("src/jmh/resources")
        compileClasspath += sourceSets["main"].runtimeClasspath
        java.destinationDirectory.set(layout.buildDirectory.dir("jmhClasses"))
    }
}

tasks.register<JavaExec>("jmh") {
    dependsOn("jmhClasses")
    mainClass.set("org.openjdk.jmh.Main")
    classpath = sourceSets.named("jmh").get().compileClasspath + sourceSets.named("jmh").get().runtimeClasspath
}

dependencies {
    "jmhAnnotationProcessor"("org.openjdk.jmh:jmh-generator-annprocess:1.27")
    "jmhImplementation"("org.openjdk.jmh:jmh-core:1.27")
    "jmhImplementation"("org.openjdk.jmh:jmh-generator-annprocess:1.27")
}

// following tasks use the output of jmh, so declare explicit dependency
listOf(
    Checkstyle::class,
    Pmd::class,
    Javadoc::class,
    com.github.spotbugs.snom.SpotBugsTask::class
).forEach { taskClass ->
    tasks.withType(taskClass.java).configureEach {
        dependsOn(":engine:compileJmhJava")
    }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// General IDE customization                                                                                         //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

idea {
    module {
        // Change around the output a bit
        inheritOutputDirs = false
        outputDir = file("build/classes")
        testOutputDir = file("build/testClasses")
        isDownloadSources = true
    }
}

// Make sure our config file for code analytics get extracted (vulnerability: non-IDE execution of single analytic)
tasks.named("ideaModule") { dependsOn(tasks.getByPath(":extractConfig")) }
tasks.named("eclipse") { dependsOn(tasks.getByPath(":extractConfig")) }
tasks.named("check") { dependsOn(tasks.getByPath(":extractConfig")) }
