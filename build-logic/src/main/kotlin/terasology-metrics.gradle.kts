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
}

tasks.withType<Test> {
    useJUnitPlatform()

    // ignoreFailures: Specifies whether the build should break when the verifications performed by this task fail.
    ignoreFailures = true
    // showStandardStreams: makes the standard streams (err and out) visible at console when running tests
    testLogging.showStandardStreams = true
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
