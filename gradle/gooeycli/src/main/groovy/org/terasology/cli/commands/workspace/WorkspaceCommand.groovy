// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.workspace

import org.terasology.cli.commands.BaseCommandType
import picocli.CommandLine.HelpCommand
import picocli.CommandLine.Command

@Command(name = "workspace",
    subcommands = [
        ListCommand.class,
        SnapshotCommand.class,
        OpenCommand.class,
        HelpCommand.class,
        RestoreCommand.class
    ],
    description = "workspace command")
class WorkspaceCommand extends BaseCommandType {
}
