// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands

import org.terasology.cli.util.Constants

import static picocli.CommandLine.Help.Ansi.AUTO

class ModuleUtil {
    static List<ModuleItem> downloadedModules() {
        List<ModuleItem> result = []
        Constants.ModuleDirectory.eachDir({ dir ->
            result << new ModuleItem(dir.getName())
        })
        return result
    }

    static ModuleItem module(String module) {
        return new ModuleItem(module)
    }

    static void copyInTemplates(ModuleItem target) {
        // Copy in the template build.gradle for modules
        println "In copyInTemplateFiles for module ${target.name()} - copying in a build.gradle then next checking for module.txt"
        File targetBuildGradle = new File(target.getDirectory(), 'build.gradle')
        targetBuildGradle.delete()
        targetBuildGradle << new File('templates/build.gradle').text

        // Copy in the template module.txt for modules (if one doesn't exist yet)
        File moduleManifest = new File(target.getDirectory(), ModuleItem.ModuleCfg)
        if (!moduleManifest.exists()) {
            def moduleText = new File("templates/module.txt").text
            moduleManifest << moduleText.replaceAll('MODULENAME', target.name())
            println AUTO.string("@|red WARNING: the module ${target.name()} did not have a module.txt! One was created, please review and submit to GitHub|@")
        }

    }

}
