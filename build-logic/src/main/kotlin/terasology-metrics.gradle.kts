// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import com.github.spotbugs.snom.SpotBugsExtension
import com.github.spotbugs.snom.SpotBugsTask

plugins {
    java
    checkstyle
    pmd
    `project-report`
    id("com.github.spotbugs")
    id("org.sonarqube")
}

dependencies {
    "pmd"("net.sourceforge.pmd:pmd-core:6.15.0")
    "pmd"("net.sourceforge.pmd:pmd-java:6.15.0")

    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.11") {
        because("runtime: to configure logging during tests with logback.xml")
    }
    testRuntimeOnly("org.codehaus.janino:janino:3.1.7") {
        because("allows use of EvaluatorFilter in logback.xml")
    }
    testRuntimeOnly("org.slf4j:jul-to-slf4j:1.7.36") {
        because("redirects java.util.logging (from e.g. junit) through slf4j")
    }

    add("testImplementation", platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("org.mockito:mockito-inline:3.12.4")
    testImplementation("org.mockito:mockito-junit-jupiter:3.12.4")

    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("com.google.truth.extensions:truth-java8-extension:1.1.3")

    testImplementation("io.projectreactor:reactor-test:3.4.14")
}

tasks.withType<Test> {
    useJUnitPlatform()

    // ignoreFailures: Specifies whether the build should break when the verifications performed by this task fail.
    ignoreFailures = true
    // showStandardStreams: makes the standard streams (err and out) visible at console when running tests
    // If false, the outputs are still collected and visible in the test report, but they don't spam the console.
    testLogging.showStandardStreams = false
    reports {
        junitXml.isEnabled = true
    }
    jvmArgs("-Xms512m", "-Xmx1024m")

    // Make sure the natives have been extracted, but only for multi-workspace setups (not for solo module builds)
    if (project.name != project(":").name) {
        dependsOn(tasks.getByPath(":extractNatives"))
    }
}

// The config files here work in both a multi-project workspace (IDEs, running from source) and for solo module builds
// Solo module builds in Jenkins get a copy of the config dir from the engine harness so it still lives at root/config
// TODO: Maybe update other projects like modules to pull the zipped dependency so fewer quirks are needed in Jenkins
configure<CheckstyleExtension> {
    isIgnoreFailures = false

    toolVersion = "10.2"

    val checkstyleDir = rootDir.resolve("config/metrics/checkstyle")
    configDirectory.set(checkstyleDir)
    setConfigProperties("samedir" to checkstyleDir)
}

configure<PmdExtension> {
    isIgnoreFailures = true
    ruleSetFiles = files(rootDir.resolve("config/metrics/pmd/pmd.xml"))
    // By default, gradle uses both ruleset file AND the rulesets. Override the ruleSets to use only those from the file
    ruleSets = listOf()
}

configure<SpotBugsExtension> {
    // The version of the spotbugs tool https://github.com/spotbugs/spotbugs
    // not necessarily the same as the version of spotbugs-gradle-plugin
    toolVersion.set("4.7.0")
    ignoreFailures.set(true)
    excludeFilter.set(file(rootDir.resolve("config/metrics/findbugs/findbugs-exclude.xml")))
}

tasks.named<SpotBugsTask>("spotbugsMain") {
    reports.register("xml") {
        enabled = true
    }
}
