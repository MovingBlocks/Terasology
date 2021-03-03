// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology

import org.gradle.api.Named
import org.gradle.api.Project

/**
 * Get the attribute value with the given name.
 *
 * As seen in [Sharing Output Between Projects](https://docs.gradle.org/current/userguide/cross_project_publications.html#sec:variant-aware-sharing).
 */
inline fun <reified T: Named> Project.namedAttribute(value: String) = objects.named(T::class.java, value)

const val JAR_COLLECTION = "jar-collection"
