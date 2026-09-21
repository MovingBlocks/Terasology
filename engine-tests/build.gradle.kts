// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

// Engine tests are split out due to otherwise quirky project dependency issues with module tests extending engine tests
// while locally all tests should be run, when building via github, tests can be run separated in the github pipeline
// file. For integration tests, a tag "flaky" was introduced to mark tests which frequently fail pipelines
//      gradle test
//      gradle --console=plain unitTest
//      gradle --console=plain integrationTest
//      gradle --console=plain integrationTestFlaky

plugins {
    id("java-library")
    id("org.jetbrains.gradle.plugin.idea-ext")
    id("terasology-common")
}

// Grab all the common stuff like plugins to use, artifact repositories, code analysis config
apply(from = "$rootDir/config/gradle/publish.gradle")

// Read environment variables, including variables passed by jenkins continuous integration server
val env = System.getenv()

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Java Section                                                                                                      //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// Read the internal version out of the engine-tests module.txt
val moduleFile = layout.projectDirectory.file("src/main/resources/org/terasology/unittest/module.txt").asFile

println("Scanning for version in module.txt for engine-tests")
@Suppress("UNCHECKED_CAST")
val moduleConfig = groovy.json.JsonSlurper().parseText(moduleFile.readText()) as Map<String, String>

// Gradle uses the magic version variable when creating the jar name (unless explicitly set differently)
version = moduleConfig["version"]!!

// Jenkins-Artifactory integration catches on to this as part of the Maven-type descriptor
group = "org.terasology.engine"

println("Version for $project.name loaded as $version for group $group")

configure<SourceSetContainer> {
    // Adjust output path (changed with the Gradle 6 upgrade, this puts it back)
    main { java.destinationDirectory.set(layout.buildDirectory.dir("classes")) }
    test { java.destinationDirectory.set(layout.buildDirectory.dir("testClasses")) }
}

// Primary dependencies definition
dependencies {
    // Dependency on the engine itself
    implementation(project(":engine"))

    // Dependency not provided for modules, but required for module-tests
    implementation(libs.gson)
    implementation("org.codehaus.plexus:plexus-utils:3.0.16")
    implementation("com.google.protobuf:protobuf-java:${libs.versions.protobuf.get()}")
    implementation("org.terasology:reflections:0.9.12-MB")

    implementation("com.github.zafarkhaja:java-semver:0.10.2")

    annotationProcessor(libs.gestalt.injectjava)
    testAnnotationProcessor(libs.gestalt.injectjava)

    implementation("org.terasology.joml-ext:joml-test:0.1.0")

    testImplementation(libs.logback) {
        because("implementation: a test directly uses logback.classic classes")
    }
    testImplementation("dev.dirs:directories:26") {
        because("PathManagerTest checks default log/module-cache paths against the same OS-standard locations PathManager computes them from")
    }


    // Test lib dependencies
    api(libs.junit.api) {
        because("we export jupiter Extensions for module tests")
    }
    api("com.google.truth:truth:1.1.3") {
        because("we provide some helper classes")
    }
    implementation(libs.mockito.core) {
        because("classes like HeadlessEnvironment use mocks")
    }
    constraints {
        implementation("net.bytebuddy:byte-buddy:1.18.11") {
            because("Mockito's inline mock maker needs a byte-buddy that recognizes newer JDK class file versions")
        }
        implementation("net.bytebuddy:byte-buddy-agent:1.18.11") {
            because("keep in sync with byte-buddy")
        }
    }

    // See terasology-metrics for other test-only internal dependencies
}

//TODO: Remove it  when gestalt will can to handle ProtectionDomain without classes (Resources)
tasks.register<Copy>("copyResourcesToClasses") {
    from("processResources")
    into(sourceSets["main"].output.classesDirs.first())
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn("copyResourcesToClasses")
    // Create an asset list during compilation (needed for Gestalt 8)
    inputs.files(sourceSets.main.get().resources.srcDirs)
    options.compilerArgs = arrayListOf("-Aresource=${sourceSets.main.get().resources.srcDirs.joinToString(File.pathSeparator)}")
}
tasks.named<JavaCompile>("compileTestJava") {
    dependsOn("copyResourcesToClasses")
    // Create an asset list during compilation (needed for Gestalt 8)
    inputs.files(sourceSets.test.get().resources.srcDirs)
    options.compilerArgs = arrayListOf("-Aresource=${sourceSets.test.get().resources.srcDirs.joinToString(File.pathSeparator)}")
}

tasks.withType<Jar> {
    // Workaround about previous copy to classes. idk why engine-tests:jar called before :engine ...
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Tests that touch real user directories rather than a @TempDir. Opt in with
// `gradlew :engine-tests:filesystemSideEffectTest` - see that task below.
val filesystemSideEffects = "filesystem-side-effects"

// Diagnostic/manual tests - report timings for comparison, not pass/fail checks. Not run
// automatically anywhere. Opt in with `gradlew :engine-tests:integrationTestDiagnostic`,
// or target one directly: `gradlew :engine-tests:integrationTestDiagnostic --tests ManyUsersChunkLoadTest`.
val diagnostic = "diagnostic"

tasks.named<Test>("test") {
    dependsOn(tasks.getByPath(":extractNatives"))
    description = "Runs all tests (slow)"
    useJUnitPlatform {
        excludeTags(filesystemSideEffects, diagnostic)
    }
    systemProperty("junit.jupiter.execution.timeout.default", "4m")
}

tasks.register<Test>("unitTest") {
    dependsOn(tasks.getByPath(":extractNatives"))
    group =  "Verification"
    description = "Runs unit tests (fast)"
    useJUnitPlatform {
        excludeTags("MteTest", "TteTest", filesystemSideEffects, diagnostic)
    }
    systemProperty("junit.jupiter.execution.timeout.default", "1m")
}

tasks.register<Test>("filesystemSideEffectTest") {
    dependsOn(tasks.getByPath(":extractNatives"))
    group = "Verification"
    description = "Runs tests that write outside a @TempDir, into real user directories. Opt-in."
    useJUnitPlatform {
        includeTags(filesystemSideEffects)
    }
    systemProperty("junit.jupiter.execution.timeout.default", "1m")
}

tasks.register<Test>("integrationTest") {
    dependsOn(tasks.getByPath(":extractNatives"))
    group = "Verification"
    description = "Runs integration tests (slow) tagged with 'MteTest' or 'TteTest', exclude tests tagged 'flaky'."

    useJUnitPlatform {
        excludeTags("flaky", diagnostic)
        includeTags("MteTest", "TteTest")
    }
    systemProperty("junit.jupiter.execution.timeout.default", "5m")
}

tasks.register<Test>("integrationTestFlaky") {
    dependsOn(tasks.getByPath(":extractNatives"))
    group = "Verification"
    description = "Runs integration tests tagged with 'flaky' and either 'MteTest' or 'TteTest'."

    useJUnitPlatform {
        includeTags("MteTest & flaky", "TteTest & flaky")
        excludeTags(diagnostic)
    }
    systemProperty("junit.jupiter.execution.timeout.default", "5m")
}

tasks.register<Test>("integrationTestDiagnostic") {
    dependsOn(tasks.getByPath(":extractNatives"))
    group = "Verification"
    description = "Runs integration tests tagged 'diagnostic' - manual/timing tools, not pass/fail regression checks. Opt-in."

    useJUnitPlatform {
        includeTags(diagnostic)
    }
    systemProperty("junit.jupiter.execution.timeout.default", "10m")
}

idea {
    module {
        // Change around the output a bit
        inheritOutputDirs = false
        outputDir = file("build/classes")
        testOutputDir = file("build/testClasses")
        isDownloadSources = true
    }
}
