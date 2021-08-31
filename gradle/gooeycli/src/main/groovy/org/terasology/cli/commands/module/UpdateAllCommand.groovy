// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.module


import org.terasology.cli.module.Modules
import picocli.CommandLine.Command

@Command(name = "update-all", description = "update all dependenices")
class UpdateAllCommand implements Runnable {
    @Override
    void run() {
        Modules.downloadedModules()
                .each { it.update() }
    }
}
