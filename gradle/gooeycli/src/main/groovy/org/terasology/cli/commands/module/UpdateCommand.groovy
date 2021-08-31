// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.module


import org.terasology.cli.module.Modules
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

@Command(name = "update", description = "Gets one or more items directly")
class UpdateCommand implements Runnable {
    @Parameters(paramLabel = "items", arity = "1..*", description = "Target item(s) to get")
    List<String> items = []

    @Parameters(paramLabel = "force", description = "dismiss all local changes")
    boolean force

    @Parameters(paramLabel = "force", description = "reset back to the default upstream branch (develop)")
    boolean reset

    @Override
    void run() {
        Modules.resolveModules(items)
                .each { it.update() }
    }
}
