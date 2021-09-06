// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.items

import org.terasology.cli.commands.common.CheckoutCommand
import org.terasology.cli.commands.common.ExecuteCommand
import org.terasology.cli.commands.common.GetCommand
import org.terasology.cli.commands.common.RefreshCommand
import org.terasology.cli.commands.common.UpdateAllCommand
import org.terasology.cli.commands.common.UpdateCommand
import org.terasology.cli.config.Config
import org.terasology.cli.items.LibItem
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand


@Command(name = "lib",
        synopsisSubcommandLabel = "COMMAND", // Default is [COMMAND] indicating optional, but sub command here is required
        subcommands = [
                HelpCommand.class,
        ],
        description = "Sub command for interacting with modules")
class LibCommand extends ItemCommand<LibItem> {

    LibCommand() {
        super(Config.FACADE)
    }

    @Override
    LibItem create(String name) {
        return new LibItem(name)
    }
}
