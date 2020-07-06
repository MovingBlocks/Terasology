// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli

// Avoid some ugly warnings about logging itself - see http://www.slf4j.org/codes.html#StaticLoggerBinder
@Grab(group = 'org.slf4j', module = 'slf4j-nop', version = '1.6.1')

// Grab the Groovy extensions for PicoCLI - in IntelliJ Alt-ENTER on a `@Grab` to register contents for syntax highlighting
@Grab('info.picocli:picocli-groovy:4.3.2')
// TODO: Actually exists inside the Gradle Wrapper - gradle-6.4.1\lib\groovy-all-1.3-2.5.10.jar\groovyjarjarpicocli\

// TODO: Unsure if this helps or should be included - don't really need this since we execute via Groovy Wrapper anyway
@GrabExclude('org.codehaus.groovy:groovy-all')

// Needed for colors to work on Windows, along with a mode toggle at the start and end of execution in main
@Grab('org.fusesource.jansi:jansi:1.18') // TODO: Exists at 1.17 inside the Gradle Wrapper lib - can use that one?
import org.fusesource.jansi.AnsiConsole

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand

// If using local groovy files the subcommands section may highlight as bad syntax in IntelliJ - that's OK
@Command(name = "gw",
    synopsisSubcommandLabel = "COMMAND", // Default is [COMMAND] indicating optional, but sub command here is required
    subcommands = [
        HelpCommand.class, // Adds standard help options (help as a subcommand, -h, and --help)
        Module.class,
        Init.class], // Note that these Groovy classes *must* start with a capital letter for some reason
    description = "Utility system for interacting with a Terasology developer workspace")
class GooeyCLI extends BaseCommand {
    static void main(String[] args) {
        AnsiConsole.systemInstall() // enable colors on Windows - TODO: Test on not-so-Windows systems, should those not run this?
        CommandLine cmd = new CommandLine(new GooeyCLI())
        if (args.length == 0) {
            cmd.usage(System.out)
        }
        else {
            cmd.execute(args)
        }
        AnsiConsole.systemUninstall() // cleanup when done
    }
}
