// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli

@Grab(group = 'org.slf4j', module = 'slf4j-nop', version = '1.6.1')
@Grab(group = 'org.eclipse.jgit', module = 'org.eclipse.jgit', version = '5.9.0.202009080501-r')
@Grab('info.picocli:picocli-groovy:4.3.2')
@Grab('org.fusesource.jansi:jansi:1.18')
@GrabExclude('org.codehaus.groovy:groovy-all')

import org.fusesource.jansi.AnsiConsole
import org.terasology.cli.commands.BaseCommandType
import org.terasology.cli.commands.BashCompletionCommand
import org.terasology.cli.commands.module.ModuleCommand
import org.terasology.cli.commands.workspace.WorkspaceCommand
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand

// If using local groovy files without Gradle the subcommands section may highlight as bad syntax in IntelliJ - that's OK
@Command(name = "gw",
        synopsisSubcommandLabel = "COMMAND", // Default is [COMMAND] indicating optional, but sub command here is required
        subcommands = [
                HelpCommand.class, // Adds standard help options (help as a subcommand, -h, and --help)
                ModuleCommand.class,
                WorkspaceCommand.class,
                BashCompletionCommand.class,
        ], // Note that these Groovy classes *must* start with a capital letter for some reason
        description = "Utility system for interacting with a Terasology developer workspace")
class GooeyCLI extends BaseCommandType {
    static void main(String[] args) {
        AnsiConsole.systemInstall() // enable colors on Windows (doesn't hurt on Linux)
        CommandLine cmd = new CommandLine(new GooeyCLI())
        if (args.length == 0) {
            cmd.usage(System.out)
        } else {
            cmd.execute(args)
        }
        AnsiConsole.systemUninstall() // cleanup when done
    }
}
