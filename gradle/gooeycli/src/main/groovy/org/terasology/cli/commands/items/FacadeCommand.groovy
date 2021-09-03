// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.items

import org.terasology.cli.commands.common.*
import org.terasology.cli.config.Config
import org.terasology.cli.items.FacadeItem
import org.terasology.cli.items.GithubIndex
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand

@Command(name = "facade",
        synopsisSubcommandLabel = "COMMAND", // Default is [COMMAND] indicating optional, but sub command here is required
        subcommands = [
                HelpCommand.class,
                GetCommand.class,
                CheckoutCommand.class,
                ExecuteCommand.class,
                UpdateAllCommand.class,
                UpdateCommand.class,
                RefreshCommand.class
        ],
        description = "Sub command for interacting with modules")
class FacadeCommand extends ItemCommand<FacadeItem> implements GithubIndex {

    FacadeCommand() {
        super(Config.FACADE)
    }

    @Override
    FacadeItem create(String name) {
        return new FacadeItem(name)
    }
}