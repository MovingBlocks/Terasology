// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the

const val DEFAULT_MAX_HEAP_SIZE = "3G"

private val logger: Logger = Logging.getLogger("org.tersology.gradology.exec")

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


open class RunTerasology : JavaExec() {

    @Option(option="max-heap", description="Set maximum heap size (-Xmx)")
    override fun setMaxHeapSize(heapSize: String?) {
        super.setMaxHeapSize(heapSize)
    }

    init {
        group = "terasology run"

        mainClass.set(project.the<JavaApplication>().mainClass)
        workingDir = project.rootDir

        initConfig()
    }

    private fun initConfig() {
        dependsOn(project.configurations.named("natives"))
        classpath(project.the<SourceSetContainer>()["main"].runtimeClasspath)
        dependsOn(project.configurations.named("modules"))

        args("-homedir")
        maxHeapSize = DEFAULT_MAX_HEAP_SIZE

        if (isMacOS()) {
            args("-noSplash")
            jvmArgs("-XstartOnFirstThread", "-Djava.awt.headless=true")
        }
    }
}
