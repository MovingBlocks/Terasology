/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.logistics

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import javax.inject.Inject


abstract class CacheReflectionsTask: SourceTask() {
    init {
        description = "Caches reflection output to make regular startup faster. May go stale and need cleanup at times."
    }

    @get:Inject
    protected abstract val layout: ProjectLayout

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    /**
     * The thing I expect people to most want to configure is where the result ends up inside the jar,
     * e.g. `reflections.cache` or `META-INF/reflections.xml`. So the property I want to be
     * configurable is that _relative_ path from the build output directory. Does a RegularFile express
     * a relative path, or do I use a String or Path input for that?
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    fun fromSourceSet(sourceSet: SourceSet) {
        outputDir.set(layout.buildDirectory.dir("reflections"))
        outputFile.set(outputDir.file("reflections.cache"))

        source(sourceSet.output.classesDirs)
        sourceSet.output.dir(mapOf("builtBy" to this), outputDir)
    }

    @TaskAction
    fun reflect() {
        finalize()
        val reflectionBuilder = ConfigurationBuilder()
        // TODO: Do we need to iterate through everything under `source`, or if it's a set of
        //   directories, does the ConfigurationBuilder have an interface we can just feed those to?
        reflectionBuilder.addUrls(source.mapNotNull { it.toURI().toURL() })
        // TODO: Why is this filter here? It's in the copy of this I pulled from modules, but not
        //   in engine's. And why is it only `org` instead of `org.terasology`?
        reflectionBuilder.filterInputsBy(FilterBuilder.parsePackages("+org"))
        reflectionBuilder.setScanners(TypeAnnotationsScanner(), SubTypesScanner())
        val reflections = Reflections(reflectionBuilder)

        reflections.save(outputFile.get().asFile.path)
        didWork = true
    }

    @Suppress("UnstableApiUsage")
    fun finalize() {
        outputDir.finalizeValue()
        outputFile.finalizeValue()
    }
}

open class CleanCachedReflections() : Delete() {
    fun fromTask(cacheTask: TaskProvider<CacheReflectionsTask>) {
        this.setDelete(cacheTask.map { it.outputDir })
    }
}


fun applyToProject(project: Project) {
    project.configure<SourceSetContainer> {
        all {
            applyToSourceSet(project.tasks, this)
        }
    }
}


private fun applyToSourceSet(tasks: TaskContainer, sourceSet: SourceSet) {
    val cacheTask = tasks.register<CacheReflectionsTask>(sourceSet.name + "CacheReflections") {
        fromSourceSet(sourceSet)
    }
    // TODO: Is there some existing convention for creating and registering `clean*` tasks?
    tasks.register<CleanCachedReflections>(
        "clean${sourceSet.name.capitalize()}CacheReflections"
    ) {
        fromTask(cacheTask)
        val cleanTask = tasks.findByPath("clean")
        cleanTask?.dependsOn(this)
    }
}
