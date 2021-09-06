// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.module

import org.terasology.cli.config.Config
import org.terasology.cli.items.ModuleItem
import picocli.CommandLine

class Modules {

    static List<ModuleItem> downloadedModules() {
        List<ModuleItem> result = []
        Config.MODULE.directory.eachDir({ dir ->
            result << resolveModule(dir.getName())
        })
        return result
    }

    static ModuleItem resolveModule(String moduleName) {
        return new ModuleItem(moduleName)
    }

    static List<ModuleItem> resolveModules(List<String> moduleNames) {
        return moduleNames.collect {
            resolveModule(it)
        }
    }

    static void copyInTemplates(ModuleItem target) {
        // Copy in the template build.gradle for modules
        println "In copyInTemplateFiles for module ${target.name} - copying in a build.gradle then next checking for module.txt"
        File targetBuildGradle = new File(target.dir, 'build.gradle')
        targetBuildGradle.delete()
        targetBuildGradle << new File('templates/build.gradle').text

        // Copy in the template module.txt for modules (if one doesn't exist yet)
        File moduleManifest = new File(target.dir, ModuleItem.ModuleCfg)
        if (!moduleManifest.exists()) {
            def moduleText = new File("templates/module.txt").text
            moduleManifest << moduleText.replaceAll('MODULENAME', target.name)
            println CommandLine.Help.Ansi.AUTO.string("@|red WARNING: the module ${target.name} did not have a module.txt! One was created, please review and submit to GitHub|@")
        }
    }
}
