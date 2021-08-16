// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli

import groovy.json.JsonSlurper
import org.terasology.cli.util.Constants

import static picocli.CommandLine.Help.Ansi.AUTO

class ModuleItem {
    public static String ModuleCfg = "module.txt"

    private String module;
    private File dir
    private Object moduleConfig = null;

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

    private Object fetchConfig() {
        if(moduleConfig == null) {
            def slurper = new JsonSlurper()
            this.moduleConfig = slurper.parseText(moduleFile.text)
            return this.moduleConfig
        }
        return this.moduleConfig
    }

    public String version() {

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


    static List<ModuleItem> downloadedModules() {
        List<ModuleItem> result = []
        Constants.ModuleDirectory.eachDir({ dir ->
            result << new ModuleItem(dir.getName())
        })
        return result
    }

    static void copyInTemplates(ModuleItem target) {
        // Copy in the template build.gradle for modules
        println "In copyInTemplateFiles for module ${target.name()} - copying in a build.gradle then next checking for module.txt"
        File targetBuildGradle = new File(target.getDirectory(), 'build.gradle')
        targetBuildGradle.delete()
        targetBuildGradle << new File('templates/build.gradle').text

        // Copy in the template module.txt for modules (if one doesn't exist yet)
        File moduleManifest = new File(target.getDirectory(), ModuleCfg)
        if (!moduleManifest.exists()) {
            def moduleText = new File("templates/module.txt").text
            moduleManifest << moduleText.replaceAll('MODULENAME', target.name())
            println AUTO.string("@|red WARNING: the module ${target.name()} did not have a module.txt! One was created, please review and submit to GitHub|@")
        }
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
