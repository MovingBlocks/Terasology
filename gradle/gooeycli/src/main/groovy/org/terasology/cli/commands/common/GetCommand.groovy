// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.common

import org.terasology.cli.commands.items.ItemCommand
import org.terasology.cli.items.GitItem
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import picocli.CommandLine.ParentCommand

@Command(name = "get", description = "get item")
class GetCommand implements Runnable {

    @ParentCommand
    ItemCommand<GitItem> parent

    @Parameters(paramLabel = "items", arity = "1", description = "Target item(s) to get")
    List<String> items = []

    @Override
    void run() {
        String origin = parent.resolveOrigin()

       items.collect {
           parent.create(it)
       }
                .parallelStream()
                .forEach { item ->
                    def targetUrl = "https://github.com/${origin}/${item.name}"

                    try {
                        item.clone(targetUrl)
                        println CommandLine.Help.Ansi.AUTO.string("@|green Retrieving item ${item.name} from ${targetUrl}|@")
                    } catch (Exception ex) {
                        println CommandLine.Help.Ansi.AUTO.string("@|red Unable to clone ${item.name}, Skipping: ${ex.getMessage()} |@")
                    }
                }
    }
}
