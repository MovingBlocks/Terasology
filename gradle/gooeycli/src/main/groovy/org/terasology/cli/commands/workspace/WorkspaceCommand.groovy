// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.workspace

import org.terasology.cli.commands.BaseCommandType
import org.terasology.cli.commands.workspace.snapshot.SnapshotCommand
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand

@Command(name = "workspace",
        subcommands = [
                SnapshotCommand.class,
                StatusCommand.class,
                HelpCommand.class
        ],
        description = "workspace command")
class WorkspaceCommand extends BaseCommandType {
}
