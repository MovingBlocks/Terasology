// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands

import groovy.json.JsonSlurper
import org.terasology.cli.util.Constants

class ModuleItem {
    public static String ModuleCfg = "module.txt"

    private String module;
    private File dir

    ModuleItem(String module) {
        this.module = module
        this.dir = new File(Constants.ModuleDirectory, module)
    }

    String name() {
        return this.module
    }

    File getDirectory() {
        return this.dir
    }

    boolean isValidModule() {
        return this.dir.exists()
    }

    ModuleItem[] dependencies(boolean respectExcludedItems = true) {
        def dependencies = []
        File moduleFile = new File(this.directory, ModuleCfg)
        def slurper = new JsonSlurper()
        def moduleConfig = slurper.parseText(moduleFile.text)
        for (dependency in moduleConfig.dependencies) {
            if (!(respectExcludedItems && Constants.ExcludeModule.contains(dependency.id))) {
                dependencies << new ModuleItem(dependency.id)
            }
        }
        return dependencies
    }

    @Override
    int hashCode() {
        return this.dir.hashCode()
    }

    @Override
    boolean equals(Object obj) {
        if (obj instanceof ModuleItem) {
            return obj.module == this.module
        }
        return super.equals(obj)
    }
}
