// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.items

import org.terasology.cli.commands.common.*
import org.terasology.cli.config.Config
import org.terasology.cli.items.GithubIndex
import org.terasology.cli.items.MetaItem
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand

@Command(name = "meta",
        synopsisSubcommandLabel = "COMMAND", // Default is [COMMAND] indicating optional, but sub command here is required
        subcommands = [
                HelpCommand.class,
                GetCommand.class,
                CheckoutCommand.class,
                ExecuteCommand.class,
                UpdateAllCommand.class,
                UpdateCommand.class
        ],
        description = "Sub command for interacting with modules")
class MetaCommand extends ItemCommand<MetaItem> implements GithubIndex {

    MetaCommand() {
        super(Config.META)
    }

    @Override
    MetaItem create(String name) {
        return new MetaItem(name)
    }
}