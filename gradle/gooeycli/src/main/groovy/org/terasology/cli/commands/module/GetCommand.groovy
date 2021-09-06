// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.module

import org.terasology.cli.commands.items.ItemCommand
import org.terasology.cli.items.ModuleItem
import org.terasology.cli.module.ModuleIndex
import org.terasology.cli.module.Modules
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.ParentCommand

@Command(name = "get", description = "get module ")
class GetCommand implements Runnable {

    @ParentCommand
    ItemCommand<ModuleItem> parent

    @Option(names = ["-r", "-recurse"], description = "recursively fetch modules")
    boolean recurse

    @Parameters(paramLabel = "items", arity = "1", description = "Target item(s) to get")
    List<String> items = []

    @Override
    void run() {
        String origin = parent.resolveOrigin()

        def modulesCandidatesToReceive = Modules.resolveModules(availableModules()*.id) - parent.listLocal()

        def requestedModules
        if (recurse) {
            requestedModules = gatherAllDependencies(Modules.resolveModules(items)).unique()
        } else {
            requestedModules = Modules.resolveModules(items)
        }

        println requestedModules
        def needsToReceive = requestedModules.intersect(modulesCandidatesToReceive)

        needsToReceive
                .parallelStream()
                .forEach { module ->
                    def targetUrl = "https://github.com/${origin}/${module.name}"
                    try {
                        module.clone(targetUrl)
                        println CommandLine.Help.Ansi.AUTO.string("@|green Retrieving module ${module.name} from ${targetUrl}|@")
                        module.copyInGradleTemplate()
                    } catch (Exception ex) {
                        println CommandLine.Help.Ansi.AUTO.string("@|red Unable to clone ${module.name}, Skipping: ${ex.getMessage()} |@")
                    }
                }
    }

    private List<ModuleItem> gatherAllDependencies(List<ModuleItem> items) {
        List<ModuleItem> moduleNames = availableModules()
                .findAll { it.id in items }
                .collect { it.dependencies*.id }
                .flatten()
                .collect { Modules.resolveModule(it) }

        if (moduleNames.empty) {
            return items
        } else {
            return items + moduleNames + gatherAllDependencies(moduleNames)
        }
    }


    private static List<Object> availableModules() {
        return ModuleIndex.instance.getData()
    }

}
