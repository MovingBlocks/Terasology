// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology

import org.gradle.api.Project
import java.io.File

class ModuleInfoException(
    cause: Throwable,
    @Suppress("MemberVisibilityCanBePrivate") val file: File? = null,
    private val project: Project? = null
) : RuntimeException(cause) {
    override val message: String
        get() {
            // trying to get the fully-qualified-class-name-mess off the front and just show
            // the useful part.
            val detail = cause?.cause?.localizedMessage ?: cause?.localizedMessage
            return "Error while reading module info from ${describeFile()}:\n  ${detail}"
        }

    private fun describeFile(): String {
        return if (project != null && file != null) {
            project.rootProject.relativePath(file)
        } else if (file != null) {
            file.toString()
        } else {
            "[unnamed file]"
        }
    }

    override fun toString(): String {
        val causeType = cause?.let { it::class.simpleName }
        return "ModuleInfoException(file=${describeFile()}, cause=${causeType})"
    }
}
