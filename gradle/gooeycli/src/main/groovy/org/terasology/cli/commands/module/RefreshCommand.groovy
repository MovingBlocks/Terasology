// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.module


import org.terasology.cli.module.Modules
import picocli.CommandLine.Command

@Command(name = "refresh", description = "Refreshes all build.gradle files in module directories")
class RefreshCommand implements Runnable {
    @Override
    void run() {
        Modules.downloadedModules().each { it ->
            if (!it.moduleCfgExists()) {
                println "${it.name} has no module.txt, it must not want a fresh build.gradle"
                return
            }
            println "In refreshGradle for module ${it.dir} - copying in a fresh build.gradle"
            it.copyInGradleTemplate()
        }
    }
}
