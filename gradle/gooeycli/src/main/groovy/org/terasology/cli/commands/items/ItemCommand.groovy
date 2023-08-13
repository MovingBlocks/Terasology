// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.items

import org.eclipse.jgit.diff.DiffEntry
import org.terasology.cli.commands.BaseCommandType
import org.terasology.cli.config.ItemConfig
import org.terasology.cli.items.GitItem
import org.terasology.cli.options.GitOptions
import picocli.CommandLine

import java.util.concurrent.TimeUnit

abstract class ItemCommand<T> extends BaseCommandType implements GitOptions {
    final ItemConfig config

    ItemCommand(ItemConfig config) {
        this.config = config
    }

    @CommandLine.Command(name = "get")
    def get(
            @CommandLine.Parameters(paramLabel = "items", arity = "1", description = "Target item(s) to get")
                    List<String> items
    ) {
        String origin = resolveOrigin()

        (items.collect {
            create(it)
        } as List<GitItem>)
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

    @CommandLine.Command(name = "checkout")
    def checkout(
            @CommandLine.Parameters(paramLabel = "items", arity = "1..*", description = "Target item(s) to execute against")
                    List<String> items,
            @CommandLine.Parameters(paramLabel = "branch", description = "target branch")
                    String branch) {
        List<GitItem> targetItem = items?.collect { create(it) } ?: listLocal()

        targetItem
                .each { it.checkout(branch) }
    }

    @CommandLine.Command(name = "update")
    def update(
            @CommandLine.Parameters(paramLabel = "items", arity = "0..*", description = "Target item(s) to execute against")
                    List<String> items
    ) {
        ((items.collect { create(it) } ?: listLocal()) as List<GitItem>)
                .findAll { !it.remote } // TODO notify about invalid item
                .each { it.update() }
    }

    @CommandLine.Command(name = "cmd")
    def execute(
            @CommandLine.Parameters(paramLabel = "items", arity = "0..*", description = "Target item(s) to execute against")
                    List<String> items,
            @CommandLine.Option(names = ["-only-modified"], description = "only execute the command on modules that were modified")
                    boolean modified,
            @CommandLine.Option(names = ["--cmd", "-c"], required = true)
                    String cmd
    ) {
        List<GitItem> targetItem = (items.collect { create(it) } ?: listLocal()) as List<GitItem>

        targetItem.each { it ->
            if (it.remote) {
                println CommandLine.Help.Ansi.AUTO.string("@|yellow Item not downloaded ${it.name} - skipping|@")
                return
            }

            if (modified) {
                List<DiffEntry> result = it.diff()
                if (result.size() == 0) {
                    return
                }
            }

            println "'${cmd}' executed in ${it.dir.toString()}"
            Process process = cmd.execute([], it.dir)
            while (!process.waitFor(1, TimeUnit.SECONDS)) {
                process.getInputStream().transferTo(System.out)
                process.getErrorStream().transferTo(System.out)
            }
            process.getInputStream().transferTo(System.out)
            process.getErrorStream().transferTo(System.out)
            println "exited with ${process.waitFor()}"
        }
    }


    abstract T create(String name)

    List<T> listLocal() {
        List<T> result = []
        config.directory.eachDir({ dir ->
            def name = dir.getName()
            if (!(name in config.excludes)) {
                result << create(name)
            }
        })
        return result
    }

}
