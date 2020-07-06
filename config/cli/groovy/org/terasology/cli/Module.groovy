// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli

import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand

// If using local groovy files the subcommands section may highlight as bad syntax in IntelliJ - that's OK
@Command(name = "module",
        synopsisSubcommandLabel = "COMMAND", // Default is [COMMAND] indicating optional, but sub command here is required
        subcommands = [
                HelpCommand.class, // Adds standard help options (help as a subcommand, -h, and --help)
                Recurse.class,
                Update.class,
                Get.class], // Note that these Groovy classes *must* start with a capital letter for some reason
        description = "Sub command for interacting with modules")
class Module extends ItemCommand {
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
