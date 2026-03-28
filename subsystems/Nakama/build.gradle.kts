// Nakama subsystem - optional Bifrost integration
// Bridges Gestalt chat events to/from a Nakama chat channel

plugins {
    java
    `java-library`
    id("terasology-common")
}

apply(from = "$rootDir/config/gradle/common.gradle")

configure<SourceSetContainer> {
    main { java.destinationDirectory.set(layout.buildDirectory.dir("classes")) }
    test { java.destinationDirectory.set(layout.buildDirectory.dir("testClasses")) }
}

repositories {
    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation(project(":engine"))

    annotationProcessor(libs.gestalt.injectjava)

    api("com.github.heroiclabs.nakama-java:nakama-java:2.5.3")
}

tasks.named<Test>("test") {
    // Exclude integration tests by default — they need a running Nakama server
    useJUnitPlatform {
        if (!project.hasProperty("includeTags")) {
            excludeTags("integration")
        } else {
            includeTags(project.property("includeTags") as String)
        }
    }
    // Pass nakama.test.* system properties through to the test JVM
    System.getProperties().entries
        .filter { (it.key as String).startsWith("nakama.test.") }
        .forEach { systemProperty(it.key as String, it.value) }
}
