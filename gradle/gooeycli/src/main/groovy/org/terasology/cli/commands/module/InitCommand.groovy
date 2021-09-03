// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli.commands.module

import org.terasology.cli.commands.BaseCommandType
import org.terasology.cli.commands.items.ItemCommand
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Ansi
import picocli.CommandLine.Parameters

// Is in use, IDE may think the Groovy-supplied is in use below and mark this unused

@Command(name = "init", description = "Initializes a workspace with some useful things")
class InitCommand extends BaseCommandType implements Runnable {

    @CommandLine.ParentCommand
    ItemCommand parent

    @Parameters(paramLabel = "distro", arity = "1..*", defaultValue = "iota", description = "Target module distro to prepare locally")
    String[] distros = []

    void run() {
        distros.each { distro ->
            println Ansi.AUTO.string("@|bold,green,underline Time to initialize ${distro}!|@")
            String origin = parent.resolveOrigin()
            def targetDistroURL = "https://raw.githubusercontent.com/$origin/Index/master/distros/$distro/gradle.properties"
            URL distroContent = new URL(targetDistroURL)
            Properties property = new Properties()
            distroContent.withInputStream { strm ->
                property.load(strm)
            }

            if (property.containsKey("extraModules")) {
                String modules = property.get("extraModules")
                GetCommand cmd = new GetCommand()
                cmd.recurse = true
                cmd.parent = parent
                cmd.items = modules.split(",").toList()
                cmd.run()
            } else {
                println "[init] ERROR: Distribution does not contain key: \"extraModules=\""
            }
        }
    }
}
