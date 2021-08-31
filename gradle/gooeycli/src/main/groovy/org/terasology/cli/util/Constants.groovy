// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.util

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class Constants {

    public static final File ModuleDirectory

    public static final String ExcludeModule = ["engine", "Index", "out", "build"]

    public static final String DefaultModuleGithubOrg = "Terasology"

    public static final String DefaultOrigin = "develop"

    public static final File FacadeDirectory

    public static final String ExcludeFacades = ["PC", "TeraEd"]

    public static final File GradlePropertyFile

    public static final Properties ProjectProperties = new Properties()

    public static final String[] DefaultModule = ["CoreSampleGameplay"]

    public static final Path ConfigurationPath

    public static final File ModuleCacheFile

    public static final long ModuleCacheValidTime = TimeUnit.DAYS.toSeconds(10)

    public static final String ModuleIndexUrl = 'http://meta.terasology.org/modules/list/latest'

    static {
        ConfigurationPath = Paths.get(System.getProperty("user.home")).resolve(".terasology")
        GradlePropertyFile = new File("gradle.properties")
        FacadeDirectory = new File("facade")
        ModuleDirectory = new File("modules")

        ModuleCacheFile = new File(".moduleCache")

        File propertyFile = new File("gradle.properties")
        if (propertyFile.exists()) {
            propertyFile.withInputStream {
                ProjectProperties.load(it)
            }
        }
    }
}
