// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.module

import groovy.io.FileType
import org.eclipse.jgit.api.Git
import org.terasology.cli.ModuleItem
import org.terasology.cli.options.GitOptions
import org.terasology.cli.util.Constants
import org.terasology.cli.util.ModuleIndex
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Command(name = "get", description = "get module ")
class GetCommand implements Runnable {

    @Mixin
    GitOptions gitOptions

    @Option(names = ["-r", "-recurse"], description = "recursively fetch modules")
    boolean recurse

    @Parameters(paramLabel = "items", arity = "1", description = "Target item(s) to get")
    List<String> items = []

    private static List<String> availableModuleIds() {
        return availableModules()*.id
    }

    private static List<Object> availableModules() {
        return ModuleIndex.instance.getData()
    }

    private static List<String> modulesAtModuleDir() {
        def list = []
        list << "engine"
        Constants.ModuleDirectory.eachFile(FileType.DIRECTORIES) { file ->
            list << file.getName()
        }
        return list as List<String>
    }

    private List<String> gatherAllDependencies(List<String> items) {
        List<String> list = availableModules()
                .findAll { it.id in items }
                .collect { it.dependencies*.id }
                .flatten()

        if (list.empty) {
            return items;
        } else {
            return items + list + gatherAllDependencies(list)
        }
    }

    @Override
    void run() {
        String origin = gitOptions.resolveOrigin()

        def modulesCandidatesToReceive = availableModuleIds() - modulesAtModuleDir()
        def requestedModules

        if (recurse) {
            requestedModules = gatherAllDependencies(items).unique()
        } else {
            requestedModules = items
        }

        def needsToReceive = requestedModules.intersect(modulesCandidatesToReceive)

        needsToReceive.collect {
            k -> new ModuleItem(k)
        }.parallelStream()
                .forEach { module ->
                    def targetUrl = "https://github.com/${origin}/${module.name()}"
                    try {

                        Git.cloneRepository()
                                .setURI(targetUrl)
                                .setDirectory(module.getDirectory())
                                .call()
                        println CommandLine.Help.Ansi.AUTO.string("@|green Retrieving module ${module.name()} from ${targetUrl}|@")

                        ModuleItem.copyInTemplates(module)
                    } catch (Exception ex) {
                        println CommandLine.Help.Ansi.AUTO.string("@|red Unable to clone ${module.name()}, Skipping: ${ex.getMessage()} |@");
                    }
                }
    }
}
