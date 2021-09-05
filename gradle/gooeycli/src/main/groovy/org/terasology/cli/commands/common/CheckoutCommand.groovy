// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.common

import org.terasology.cli.commands.items.ItemCommand
import org.terasology.cli.items.GitItem
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import picocli.CommandLine.ParentCommand

@Command(name = "checkout", description = "checkout specific branch")
class CheckoutCommand implements Runnable {

    @ParentCommand
    ItemCommand<GitItem> parent

    @Parameters(paramLabel = "branch", description = "target branch")
    String branch

    @Parameters(paramLabel = "items", arity = "1..*", description = "Target item(s) to execute against")
    List<String> items = []


    @Override
    void run() {
        List<GitItem> targetItem
        if (items.size() > 0) {
            targetItem = items.collect { parent.create(it) }
        } else {
            targetItem = parent.listLocal()
        }
        targetItem
                .each { it.checkout(branch) }
    }
}
