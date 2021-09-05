// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.common

import org.terasology.cli.commands.items.ItemCommand
import org.terasology.cli.items.GitItem
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

@Command(name = "update", description = "Gets one or more items directly")
class UpdateCommand implements Runnable {

    @CommandLine.ParentCommand
    ItemCommand<GitItem> parent

    @Parameters(paramLabel = "items", arity = "1..*", description = "Target item(s) to get")
    List<String> items = []

//    @Parameters(paramLabel = "force", description = "dismiss all local changes")
//    boolean force
//
//    @Parameters(paramLabel = "force", description = "reset back to the default upstream branch (develop)")
//    boolean reset

    @Override
    void run() {
        items.collect { parent.create(it) }
                .findAll { !it.remote } // TODO notify about invalid item
                .each { it.update() }
    }
}
