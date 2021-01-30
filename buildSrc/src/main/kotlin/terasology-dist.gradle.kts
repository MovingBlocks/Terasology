// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

@Suppress("UnstableApiUsage")
open class ValidateZipDistribution @Inject constructor(objects: ObjectFactory) : DefaultTask() {
    @InputFile
    val zipFile: RegularFileProperty = objects.fileProperty()

    @get:Internal
    val tree by lazy {
        zipFile.finalizeValue()
        project.zipTree(zipFile)
    }

    init {
        group = "verification"
    }

    @TaskAction
    fun checkZip() {
        zipFile.finalizeValueOnRead()
    }

    fun fromTask(taskProvider: Provider<Zip>) {
        // FIXME: is it right to use .get here?
        //   https://discuss.gradle.org/t/un-nesting-provider-provider/38904
        val fileProvider = taskProvider.map { it.archiveFile }.get()
        zipFile.set(fileProvider)
    }

    fun assertEquals(expected: Any, actual: Any, message: String? = null) =
        kotlin.test.assertEquals(expected, actual, message)

    fun fail(message: String): Nothing = kotlin.test.fail(message)

    fun assertContainsPath(expected: String) {
        assertEquals(1, tree.matching {
            include(expected)
        }.count(), "Matches for $expected")
    }
}


