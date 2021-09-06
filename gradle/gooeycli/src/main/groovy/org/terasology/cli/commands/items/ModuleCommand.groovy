// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli.commands.items


import org.terasology.cli.config.Config
import org.terasology.cli.items.GradleItem
import org.terasology.cli.items.ModuleItem
import org.terasology.cli.module.ModuleIndex
import org.terasology.cli.module.Modules
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand


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
        (listLocal() as GradleItem).each { it ->
            // TODO
//            if (!it.moduleCfgExists()) {
//                println "${it.name} has no module.txt, it must not want a fresh build.gradle"
//                return
//            }
            println "In refreshGradle for module ${it.dir} - copying in a fresh build.gradle"
            it.copyInGradleTemplate()
        }
    }

    @Command(name = "recurse")
    def recurse(@CommandLine.Parameters(paramLabel = "items", arity = "1", description = "Target item(s) to get")
                        List<String> items
    ) {
        String origin = resolveOrigin()

        def modulesCandidatesToReceive = Modules.resolveModules(availableModules()*.id) - listLocal()

        def requestedModules = gatherAllDependencies(Modules.resolveModules(items)).unique()

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
                recurse(modules.split(",").toList())
            } else {
                println "[init] ERROR: Distribution does not contain key: \"extraModules=\""
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
