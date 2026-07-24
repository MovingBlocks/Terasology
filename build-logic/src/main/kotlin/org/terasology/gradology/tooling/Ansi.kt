// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology.tooling

/**
 * Small ANSI coloring utility.
 * @see <a href="https://gist.github.com/tvinke/db4d21dfdbdae49e6f92dcf1ca6120de">reference</a>
 */
object Ansi {
    const val NORMAL = "[0m"

    const val RED = "[31m"
    const val GREEN = "[32m"
    const val YELLOW = "[33m"
    const val LIGHT_YELLOW = "[1;33m"

    fun color(text: String, ansiValue: String): String = ansiValue + text + NORMAL
}
