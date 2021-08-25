// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.workspace.snapshot

import org.terasology.cli.Snapshot
import picocli.CommandLine.Command

@Command(name = "list", description = "list all snapshots")
class ListCommand implements Runnable{
    @Override
    void run() {
        if(!Snapshot.SnapshotDirectory.exists()) {
            println "No snapshots exist"
            return
        }
        def snapshots = Snapshot.currentSnapshots()
        if(snapshots.size() == 0) {
            println "No snapshots exist"
        }
        snapshots.eachWithIndex {it, index ->
            if(it.tag?.trim()) {
                println "${index}: ${it.tag} - ${it.captured.toString()}"
            } else {
                println "${index}: ${it.captured.toString()}"
            }
        }
    }
}
