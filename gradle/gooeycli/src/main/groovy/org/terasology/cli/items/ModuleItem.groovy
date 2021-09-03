// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.items

import groovy.json.JsonSlurper
import org.terasology.cli.module.Modules
import org.terasology.cli.config.Config
import org.terasology.cli.config.GradleAwareConfig

class ModuleItem extends Item implements GitItem<ModuleItem>, GradleItem<GradleAwareConfig> {
    public static String ModuleCfg = "module.txt"


    ModuleItem(String module) {
        super(module, new File(Config.MODULE.directory, module) )

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
            if (!(respectExcludedItems && Config.ExcludeModule.contains(dependency.id))) {
                dependencies << Modules.resolveModules(dependency.id)
            }
        }
        return dependencies
    }

    @Override
    GradleAwareConfig getConfig() {
        return Config.MODULE
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
        return Object.equals(obj)
    }

    @Override
    String toString() {
        return "ModuleItem{$name}"
    }
}
