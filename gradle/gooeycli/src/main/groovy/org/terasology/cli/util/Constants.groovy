// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.util

class Constants {

    public static final File ModuleDirectory;

    public static final String ExcludeModule = ["engine", "Index", "out", "build"];

    public static final String DefaultModuleGithubOrg = "Terasology"

    public static final String DefaultOrigin = "develop"

    public static final File FacadeDirectory;

    public static final String ExcludeFacades = ["PC", "TeraEd"];

    public static final File GradlePropertyFile;

    public static final Properties ProjectProperties = new Properties();

    public static final String[] DefaultModule = ["CoreSampleGameplay"]

    static {
        GradlePropertyFile = new File("gradle.properties")
        FacadeDirectory = new File("facade");
        ModuleDirectory = new File("modules");

        File propertyFile = new File("gradle.properties")
        if (propertyFile.exists()) {
            propertyFile.withInputStream {
                ProjectProperties.load(it)
            }
        }
    }
}
