// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.module

import org.terasology.cli.ModuleItem
import picocli.CommandLine.Command

@Command(name = "refresh", description = "Refreshes all build.gradle files in module directories")
class RefreshCommand implements Runnable {
    @Override
    void run() {
        ModuleItem.downloadedModules().each { it ->
            if (!new File(it.getDirectory(), ModuleItem.ModuleCfg).exists()) {
                println "${it.name()} has no module.txt, it must not want a fresh build.gradle"
                return
            }
            println "In refreshGradle for module ${it.getDirectory()} - copying in a fresh build.gradle"
            File targetBuildGradle = new File(it.getDirectory(), 'build.gradle')
            targetBuildGradle.delete()
            targetBuildGradle << new File('templates/build.gradle').text
        }
    }
}
