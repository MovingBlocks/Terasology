// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli.options

import org.terasology.cli.config.Config
import org.terasology.cli.config.ItemConfig
import picocli.CommandLine.Option

/**
 * A trait meant to supply some general Git-related options
 */
trait GitOptions {

    abstract ItemConfig getConfig()

    @Option(names = ["-origin", "-o"], description = "Github remote Organization/User to target")
    String origin

    @Option(names = ["-b"], description = "Git branch")
    String branch

    String resolveOrigin() {
        if (origin != null) {
            return origin
        }
        if (Config.ProjectProperties.hasProperty("alternativeGithubHome")) {
            return Config.ProjectProperties.getProperty("alternativeGithubHome")
        }
        return config.defaultOrg
    }

}
