// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.workspace.snapshot

import org.terasology.cli.items.Snapshot
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

@Command(name = "load", description = "load snapshot")
class LoadCommand implements Runnable {

    @Parameters(paramLabel = "items", arity = "1", description = "restore snapshot")
    String target

    @Override
    void run() {
        File file = new File(target)
        if (!file.exists()) {
            return
        }
        Snapshot snapshot = new Snapshot(file)
        snapshot.file = new File(Snapshot.SnapshotDirectory, "${snapshot.captured.epochSecond}.snapshot")
        snapshot.save()

    }
}
