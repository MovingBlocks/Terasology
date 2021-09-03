// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.common

import org.terasology.cli.commands.items.ItemCommand
import org.terasology.cli.items.GitItem
import picocli.CommandLine
import picocli.CommandLine.Command

@Command(name = "update-all", description = "update all dependenices")
class UpdateAllCommand implements Runnable {

    @CommandLine.ParentCommand
    ItemCommand<GitItem> parent

    @Override
    void run() {
        parent.listLocal()
                .each { it.update() }
    }
}
