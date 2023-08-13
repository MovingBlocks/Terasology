// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.workspace.snapshot

import org.terasology.cli.commands.BaseCommandType
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand

@Command(name = "snapshot",
        subcommands = [
                ListCommand.class,
                CreateCommand.class,
                OpenCommand.class,
                HelpCommand.class,
                RestoreCommand.class,
                LoadCommand.class
        ],
        description = "Snapshots allows to share workspace between devs. Useful for multi-repo PRs testing")
class SnapshotCommand extends BaseCommandType {
}
