// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli
import picocli.CommandLine.Option

/**
 * A mix-in meant to supply some general Git-related options
 */
class GitOptions {
    @Option(names = [ "-o", "--origin"], description = "Which Git origin (account) to target")
    String origin
}
