// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.serializers.JsonSerializer
import org.reflections.util.ConfigurationBuilder
import java.net.URLClassLoader

tasks.register("cacheReflections") {
    description = "Caches reflection output to make regular startup faster. May go stale and need cleanup at times."

    val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
    val mainSourceSet: SourceSet = sourceSets[SourceSet.MAIN_SOURCE_SET_NAME]

    inputs.files(mainSourceSet.output.classesDirs)
    dependsOn(tasks.named("classes"))
    outputs.upToDateWhen { tasks.named("classes").get().state.upToDate }

    // TODO: output to the task's own build directory, because putting things in the
    //     classes directory confuses gradle caching.
    val manifestFile = File(mainSourceSet.output.classesDirs.first(), "manifest.json")
    outputs.file(manifestFile)

    doLast {
        val classPaths = mainSourceSet.compileClasspath.map { it.toURI().toURL() }
        val classLoader = URLClassLoader(classPaths.toTypedArray())
        try {
            val reflections = Reflections(
                ConfigurationBuilder()
                .setSerializer(JsonSerializer())
                .addClassLoader(classLoader)
                // .filterInputsBy(FilterBuilder.parsePackages("+org"))
                .addUrls(inputs.files.map { it.toURI().toURL() })
                .setScanners(TypeAnnotationsScanner(), SubTypesScanner(false)))
            reflections.save(manifestFile.toString())
        } catch (e: java.net.MalformedURLException) {
            logger.error("Cannot parse input to url", e)
        }
    }
}
