// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli.commands.items


import org.terasology.cli.config.Config
import org.terasology.cli.items.ModuleItem
import org.terasology.cli.module.Modules
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Command(name = "module",
        synopsisSubcommandLabel = "COMMAND", // Default is [COMMAND] indicating optional, but sub command here is required
        subcommands = [
                HelpCommand.class, // Adds standard help options (help as a subcommand, -h, and --help)
        ], // Note that these Groovy classes *must* start with a capital letter for some reason
        description = "Sub command for interacting with modules")
class ModuleCommand extends ItemCommand<ModuleItem> {

    ModuleCommand() {
        super(Config.MODULE)
    }

    @Override
    ModuleItem create(String name) {
        return new ModuleItem(name)
    }

    @Command(name = "refresh")
    def refresh() {
        listLocal().each { it ->
            Modules.copyInTemplates(it)
        }
    }


    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors() - 1)
    Set fetchingModules = []

    @Command(name = "recurse")
    def recurse(@CommandLine.Parameters(paramLabel = "items", arity = "1", description = "Target item(s) to get")
                        List<String> items
    ) {
       recurseInner(items).each {
           it.get() // wait there
       }
        executorService.shutdown()
    }

    List<Future<Void>> recurseInner(List<String> items) {
        String origin = resolveOrigin()
        def futures = []
        items.each {
            def module = create it
            if (module.remote && !(module.name in fetchingModules)) {
                try {
                    fetchingModules << module.name
                    def targetUrl = "https://github.com/${origin}/${module.name}"

                    module.clone(targetUrl)
                    Modules.copyInTemplates(module)

                    futures << executorService.submit {
                        futures.addAll(recurseInner(module.dependencies()))
                    }
                    fetchingModules.remove(module.name)
                    println CommandLine.Help.Ansi.AUTO.string("@|green Retrieving item ${module.name} from ${targetUrl}|@")
                } catch (Exception ex) {
                    println CommandLine.Help.Ansi.AUTO.string("@|red Unable to clone ${module.name}, Skipping: ${ex.getMessage()} |@")
                }
            } else {
                //  already cloned
            }
        }
        return futures
    }

    @Command(name = "init")
    def init(@CommandLine.Parameters(paramLabel = "distro", arity = "1..*", defaultValue = "iota", description = "Target module distro to prepare locally")
                     String[] distros) {

        distros.each { distro ->
            println CommandLine.Help.Ansi.AUTO.string("@|bold,green,underline Time to initialize ${distro}!|@")
            String origin = resolveOrigin()
            def targetDistroURL = "https://raw.githubusercontent.com/$origin/Index/master/distros/$distro/gradle.properties"
            URL distroContent = new URL(targetDistroURL)
            Properties property = new Properties()
            distroContent.withInputStream { strm ->
                property.load(strm)
            }

            if (property.containsKey("extraModules")) {
                String modules = property.get("extraModules")
                recurseInner(modules.split(",").toList()).each {
                    it.get()
                }
                executorService.shutdown()

            } else {
                println "[init] ERROR: Distribution does not contain key: \"extraModules=\""
            }
        }
    }
}
