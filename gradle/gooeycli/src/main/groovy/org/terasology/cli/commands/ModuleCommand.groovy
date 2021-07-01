// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli.commands

import org.terasology.cli.managers.ManagedItem
import org.terasology.cli.managers.ManagedModule
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand

@Command(name = "module",
        synopsisSubcommandLabel = "COMMAND", // Default is [COMMAND] indicating optional, but sub command here is required
        subcommands = [
                HelpCommand.class, // Adds standard help options (help as a subcommand, -h, and --help)
                RecurseCommand.class,
                UpdateCommand.class,
                GetCommand.class], // Note that these Groovy classes *must* start with a capital letter for some reason
        description = "Sub command for interacting with modules")
class ModuleCommand extends ItemCommandType {

    @Override
    ManagedItem getManager(String optionGitOrigin) {
        return new ManagedModule(optionGitOrigin)
    }

    // This is an example of a subcommand via method - used here so we can directly hit ManagedModule for something module-specific
    // If in an external Refresh.groovy it _could_ specify ManagedModule, but it then could be added later to a non-module and break
    @Command(name = "refresh", description = "Refreshes all build.gradle files in module directories")
    void refresh() {
        new ManagedModule().refreshGradle()
    }
}
