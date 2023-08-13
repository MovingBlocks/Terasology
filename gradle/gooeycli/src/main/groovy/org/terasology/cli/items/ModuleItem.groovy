// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.items


import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.terasology.cli.config.Config
import org.terasology.cli.config.GradleAwareConfig

@CompileStatic
class ModuleItem extends Item implements GitItem<ModuleItem>, GradleItem<GradleAwareConfig> {
    public static String ModuleCfg = "module.txt"

    ModuleItem(String module) {
        super(module, new File(Config.MODULE.directory, module))

    }

    boolean moduleCfgExists() {
        return new File(dir, ModuleItem.ModuleCfg).exists()
    }

    @CompileDynamic
    List<String> dependencies(boolean respectExcludedItems = true) {
        def dependencies = []
        Object moduleConfig = getModuleJson()
        for (dependency in moduleConfig.dependencies) {
            if (!(respectExcludedItems && config.excludes.contains(dependency.id))) {
                dependencies << dependency.id
            }
        }
        return dependencies
    }


    private Object getModuleJson() {
        File moduleFile = new File(this.dir, ModuleCfg)

        def moduleConfig = new JsonSlurper().parseText(moduleFile.text)
        moduleConfig
    }

    @Override
    GradleAwareConfig getConfig() {
        return Config.MODULE
    }

    ModuleItem copyInGradleTemplate() {
        def target = new File(dir, 'build.gradle')
        def source = new File('templates/build.gradle')
        target.write source.text
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
    @CompileDynamic
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
