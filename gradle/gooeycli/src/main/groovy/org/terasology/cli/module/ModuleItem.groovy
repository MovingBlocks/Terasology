// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.module

import groovy.json.JsonSlurper
import org.terasology.cli.traits.Gitable
import org.terasology.cli.util.Constants

class ModuleItem implements Gitable<ModuleItem> {
    public static String ModuleCfg = "module.txt"

    ModuleItem() {
        parentDir = Constants.ModuleDirectory
    }

    ModuleItem(String module) {
        this()
        this.name = module
        this.dir = new File(parentDir, module)
        remote = !dir.exists()
    }

    boolean moduleCfgExists() {
        return new File(dir, ModuleItem.ModuleCfg).exists()
    }

    ModuleItem[] dependencies(boolean respectExcludedItems = true) {
        def dependencies = []
        File moduleFile = new File(this.dir, ModuleCfg)
        def slurper = new JsonSlurper()
        def moduleConfig = slurper.parseText(moduleFile.text)
        for (dependency in moduleConfig.dependencies) {
            if (!(respectExcludedItems && Constants.ExcludeModule.contains(dependency.id))) {
                dependencies << Modules.resolveModules(dependency.id)
            }
        }
        return dependencies
    }

    ModuleItem copyInGradleTemplate() {
        new File(dir, 'build.gradle').bytes = new File('templates/build.gradle').bytes
        return this
    }

    ModuleItem copyInModuleTemplate() {
        new File(dir, ModuleItem.ModuleCfg).text == new File("templates/module.txt").text
                .replaceAll('MODULENAME', name)
        return this
    }

    @Override
    int hashCode() {
        return this.dir.hashCode()
    }

    @Override
    boolean equals(Object obj) {
        if (obj instanceof ModuleItem) {
            return obj.name == this.name
        }
        return super.equals(obj)
    }

    @Override
    String toString() {
        return "ModuleItem{$name}"
    }
}
