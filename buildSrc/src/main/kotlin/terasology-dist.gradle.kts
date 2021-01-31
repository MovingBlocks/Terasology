// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

/**
 * Make assertions about the content of a zip file.
 *
 * Do make sure you put any inspection of the file in the task's *execution phase,* not in its *configuration.*
 * i.e. use a `doLast` block.
 *
 * TODO: Produce proper test output instead of just throwing an exception and failing the build.
 *   Probably with [TestKit](https://docs.gradle.org/current/userguide/test_kit.html)?
 */
@Suppress("UnstableApiUsage")
open class ValidateZipDistribution @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    /**
     * The zip file to inspect.
     *
     * @see fromTask to set this to the output of a Zip task.
     */
    @InputFile
    val zipFile: RegularFileProperty = objects.fileProperty()

    /**
     * A tree view of the content of the zip file.
     */
    @get:Internal
    val tree: FileTree by lazy {
        zipFile.finalizeValue()
        project.zipTree(zipFile)
    }

    init {
        group = "verification"
    }

    @TaskAction
    private fun checkZip() {
        zipFile.finalizeValueOnRead()
    }

    /**
     * Use zip file output by the given task.
     */
    fun fromTask(taskProvider: Provider<Zip>) {
        zipFile.set(taskProvider.flatMap { it.archiveFile })
    }

    /**
     * Assert two objects are equal.
     *
     * @see kotlin.test.assertEquals
     */
    fun assertEquals(expected: Any, actual: Any, message: String? = null) =
        kotlin.test.assertEquals(expected, actual, message)

    /**
     * Fail this test with the given message.
     */
    fun fail(message: String): Nothing = kotlin.test.fail(message)

    /**
     * Assert the zip contains exactly one match for the given pattern.
     *
     * @param pattern: match pattern in glob-style [pattern format](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/util/PatternFilterable.html).
     */
    fun assertContainsPath(pattern: String) {
        assertEquals(1, tree.matching {
            include(pattern)
        }.count(), "Matches for $pattern")
    }
}


