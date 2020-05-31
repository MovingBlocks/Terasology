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
import org.gradle.api.file.ConfigurableFileCollection
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

const val TASK_GROUP_NAME = "Build"


abstract class CacheReflectionsTask: SourceTask() {
    init {
        group = TASK_GROUP_NAME
        description = "Caches reflection output to make regular startup faster."
    }

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

    @get:Internal
    internal var mySourceSet: SourceSet? = null

    fun configure() {
        outputFile.set(outputDir.file("reflections.cache"))
    }

    @TaskAction
    fun reflect() {
        finalize()
        val reflectionBuilder = ConfigurationBuilder()
        reflectionBuilder.setScanners(TypeAnnotationsScanner(), SubTypesScanner())

        // There's some question of how much to include in here. Too much and we get
        // a cache bloated with things we never use. But the docs also note:
        //
        // > Make sure to scan all the transitively relevant packages. For instance, given your
        // > class C extends B extends A, and both B and A are located in another package than
        // > C, when only the package of C is scanned - then querying for sub types of A returns
        // > nothing (transitive), but querying for sub types of B returns C (direct). In that case
        // > make sure to scan all relevant packages a priori.

        mySourceSet?.also { sourceSet ->
            val classesDirs = sourceSet.output.classesDirs
            reflectionBuilder.addUrls(classesDirs.map { it.toURI().toURL() })
        }

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


fun TaskProvider<CacheReflectionsTask>.provideToSourceSet(
    layout: ProjectLayout,
    sourceSet: SourceSet
) {
    val outDir = layout.buildDirectory.dir("reflections/" + sourceSet.name)
    preventSourceSetSelfDependency(sourceSet)

    sourceSet.output.dir(mapOf("builtBy" to this), outDir)

    configure {
        source(sourceSet.output.classesDirs)
        mySourceSet = sourceSet
        outputDir.set(outDir)
    }
}


/**
 * Correct DefaultSourceSetOutput dependencies.
 *
 * [org.gradle.api.internal.tasks.DefaultSourceSetOutput] declares that its classesDirs are built
 * by itself. Given that we want to _depend_ on the built classes and include our output _in_ the
 * source set, this does not work for us. They even left a comment:
 *
 * > // TODO: This should be more specific to just the tasks that create the class files?
 *
 * Yes please.
 *
 * In the meantime, we yank out that dependency and hope everything still works.
 *
 * See [source](https://github.com/gradle/gradle/blob/v6.4.1/subprojects/plugins/src/main/java/org/gradle/api/internal/tasks/DefaultSourceSetOutput.java#L49-L51)
 */
private fun preventSourceSetSelfDependency(sourceSet: SourceSet) {
    // SourceSetOutput's interface doesn't return classesDirs as mutable,
    // but DefaultSourceSetOutput does. So this probably works.
    (sourceSet.output.classesDirs as ConfigurableFileCollection).apply {
        // It complains if we try to remove an element from the existing set,
        // but we can replace it with a new set.
        setBuiltBy(builtBy.minusElement(sourceSet.output))

        // This is a guess. It isn't complete if there are non-Java languages contributing classes.
        builtBy(sourceSet.compileJavaTaskName)
    }
}


open class CleanCachedReflections : Delete() {
    init {
        group = TASK_GROUP_NAME
    }

    fun fromTask(cacheTask: TaskProvider<CacheReflectionsTask>) {
        this.setDelete(cacheTask.map { it.outputDir })
    }
}


class TaskInstaller(project: Project) {
    private val layout: ProjectLayout = project.layout
    private val tasks: TaskContainer = project.tasks

    init {
        project.configure<SourceSetContainer> {
            configureEach {
                configureSourceSet(this)
            }
        }
    }

    fun configureSourceSet(sourceSet: SourceSet) {
        val cacheTask = tasks.register<CacheReflectionsTask>("cacheReflections" + sourceSet.name.capitalize()) {
            configure()
        }
        cacheTask.provideToSourceSet(layout, sourceSet)


        // TODO: Is there some existing convention for creating and registering `clean*` tasks?
        val cleanCacheTask = tasks.register<CleanCachedReflections>(
            "cleanCacheReflections" + sourceSet.name.capitalize()
        ) {
            fromTask(cacheTask)
        }

        tasks.named("clean").configure {
            dependsOn(cleanCacheTask)
        }
    }

    companion object {
        fun reflectionsForAllSourceSets(project: Project) {
            TaskInstaller(project)
        }
    }
}
