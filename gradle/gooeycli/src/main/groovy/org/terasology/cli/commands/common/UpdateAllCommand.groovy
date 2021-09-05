// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.common

import org.terasology.cli.commands.items.ItemCommand
import org.terasology.cli.items.GitItem
import picocli.CommandLine.Command
import picocli.CommandLine.ParentCommand

@Command(name = "update-all", description = "update all dependenices")
class UpdateAllCommand implements Runnable {

    @ParentCommand
    ItemCommand<GitItem> parent

    @Override
    void run() {
        parent.listLocal()
                .each { it.update() }
    }
}
