// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli.options

import org.terasology.cli.util.Constants
import picocli.CommandLine.Option

/**
 * A mix-in meant to supply some general Git-related options
 */
class GitOptions {
    @Option(names = ["-origin", "-o"], description = "Github remote Organization/User to target", defaultValue = Constants.DefaultModuleGithubOrg)
    String origin

    @Option(names = ["-b"], description = "Git branch", defaultValue = Constants.DefaultOrigin)
    String branch

    String resolveOrigin() {
        if (!origin.isEmpty()) {
            return origin
        }
        if (Constants.ProjectProperties.hasProperty("alternativeGithubHome")) {
            return Constants.ProjectProperties.getProperty("alternativeGithubHome")
        }
        return Constants.DefaultModuleGithubOrg
    }

}
