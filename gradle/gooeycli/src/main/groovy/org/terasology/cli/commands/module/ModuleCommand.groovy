// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli.commands.module

import org.terasology.cli.commands.BaseCommandType
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand

@Command(name = "module",
        synopsisSubcommandLabel = "COMMAND", // Default is [COMMAND] indicating optional, but sub command here is required
        subcommands = [
                HelpCommand.class, // Adds standard help options (help as a subcommand, -h, and --help)
                InitCommand.class,
                GetCommand.class,
                RefreshCommand.class,
                ExecuteCommand.class,
                UpdateAllCommand.class,
        ], // Note that these Groovy classes *must* start with a capital letter for some reason
        description = "Sub command for interacting with modules")
class ModuleCommand extends BaseCommandType {

}
