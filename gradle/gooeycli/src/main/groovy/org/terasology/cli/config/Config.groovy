// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.config

import groovy.transform.CompileStatic

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

@CompileStatic
class Config {

    public static final GradleAwareConfig MODULE = new GradleAwareConfig().tap {
        directory = new File("modules")
        defaultBranch = "develop"
        defaultOrg = "Terasology"
        gradleTemplatePath = new File("templates/build.gradle")
    }

    public static final CommonConfig META = new CommonConfig().tap {
        directory = new File("metas")
        defaultBranch = "master"
        defaultOrg = "MetaTerasology"
    }

    public static final CommonConfig LIB = new  CommonConfig().tap {
        directory = new File("libs")
        defaultBranch = "develop"
        defaultOrg = "MovingBlocks"
    }

    public static final GradleAwareConfig FACADE = new GradleAwareConfig().tap {
        directory = new File("facades")
        defaultBranch = "develop"
        defaultOrg = "MovingBlocks"
        gradleTemplatePath = new File("templates/facades.gradle")
        excludes = ["PC","TeraEd"]
    }


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

        ModuleCacheFile = new File(".moduleCache")

        File propertyFile = new File("gradle.properties")
        if (propertyFile.exists()) {
            propertyFile.withInputStream {
                ProjectProperties.load(it)
            }
        }
    }
}
