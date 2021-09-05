// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.common

import org.terasology.cli.commands.items.ItemCommand
import org.terasology.cli.items.GradleItem
import picocli.CommandLine.Command
import picocli.CommandLine.ParentCommand

@Command(name = "refresh", description = "Refreshes all build.gradle files in item's directories")
class RefreshCommand implements Runnable {

    @ParentCommand
    ItemCommand<GradleItem> parent

    @Override
    void run() {
        println parent.listLocal()
        parent.listLocal().each { it ->
//            if (!it.moduleCfgExists()) {
//                println "${it.name} has no module.txt, it must not want a fresh build.gradle"
//                return
//            }
            println "In refreshGradle for module ${it.dir} - copying in a fresh build.gradle"
            it.copyInGradleTemplate()
        }
    }
}
