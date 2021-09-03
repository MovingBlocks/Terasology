// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.items

import org.terasology.cli.config.GradleAwareConfig
import org.terasology.cli.config.GradleTemplateConfig

trait GradleItem<T extends GradleAwareConfig> {

    abstract File getDir()

    abstract T getConfig()

    def copyInGradleTemplate() {
        File gradleFile = new File(dir, 'build.gradle')

        gradleFile.text = config.gradleTemplatePath.text
        return this
    }
}