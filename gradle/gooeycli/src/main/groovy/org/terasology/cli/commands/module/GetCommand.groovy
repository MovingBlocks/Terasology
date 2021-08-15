// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.module

import org.eclipse.jgit.api.Git
import org.terasology.cli.commands.ModuleItem
import org.terasology.cli.commands.ModuleUtil
import org.terasology.cli.options.GitOptions
import picocli.CommandLine
import picocli.CommandLine.Mixin
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.Command

@Command(name = "get", description = "get module ")
class GetCommand implements Runnable{
    private Set<String> fetchedModules = [];

    @Mixin
    GitOptions gitOptions

    @Option(names = ["-r", "-recurse"], description = "recursively fetch modules")
    boolean recurse

    @Parameters(paramLabel = "items", arity = "1", description = "Target item(s) to get")
    List<String> items = []

    @Override
    void run() {
        String origin = gitOptions.resolveOrigin()

        for (module in items.collect({k -> new ModuleItem(k)})) {
            def targetUrl = "https://github.com/${origin}/${module.name()}"
            if (fetchedModules.contains(targetUrl)) {
                continue
            }
            if (module.getDirectory().exists()) {
                println CommandLine.Help.Ansi.AUTO.string("@|yellow already retrieved ${module.name()} - skipping|@")
                continue
            }
            try {
                Git.cloneRepository()
                        .setURI(targetUrl)
                        .setDirectory(module.getDirectory())
                        .call()
                println CommandLine.Help.Ansi.AUTO.string("@|green Retrieving module ${module.name()} from ${targetUrl}|@")
                fetchedModules << module;
            } catch (Exception ex) {
                println CommandLine.Help.Ansi.AUTO.string("@|red Unable to clone ${module.name()}, Skipping: ${ex.getMessage()} |@");
                continue
            }

            ModuleUtil.copyInTemplates(module)
            if (recurse) {
                def dependencies = module.dependencies()
                if (dependencies.length > 0) {
                    String[] uniqueDependencies = dependencies - fetchedModules
                    println "After removing dupes already retrieved we have the remaining dependencies left: $uniqueDependencies"
                    if (uniqueDependencies.length > 0) {
                        GetCommand cmd = new GetCommand();
                        cmd.gitOptions = this.gitOptions
                        cmd.items = uniqueDependencies.toList()
                        cmd.recurse = true
                        cmd.run()
                    }
                }
            }
        }
    }
}
