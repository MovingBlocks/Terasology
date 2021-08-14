// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands


import picocli.CommandLine.Command
import picocli.CommandLine.Mixin

@Command(name = "refresh", description = "Refreshes all build.gradle files in module directories")
class ModuleRefreshCommand implements Runnable {


    @Override
    void run() {
        ModuleUtil.allModules().each { it ->
            if (!new File(it.getDirectory(), "module.txt").exists()) {
                println "${it.getModule()} has no module.txt, it must not want a fresh build.gradle"
                return
            }
            println "In refreshGradle for module ${it.getDirectory()} - copying in a fresh build.gradle"
            File targetBuildGradle = new File(it.getDirectory(), 'build.gradle')
            targetBuildGradle.delete()
            targetBuildGradle << new File('templates/build.gradle').text
        }
    }
}
