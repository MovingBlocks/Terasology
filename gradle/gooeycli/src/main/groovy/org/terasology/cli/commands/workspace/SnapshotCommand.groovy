// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.workspace


import org.terasology.cli.ModuleItem
import org.terasology.cli.Snapshot
import org.terasology.cli.SnapshotModule
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = "snapshot",
    description = "captures a snapshot of all the modules and the associated commit")
class SnapshotCommand implements Runnable {

    @Option(names = ["-tag", "-t"], description = "tag snapshot with name", required = false)
    String tag = ""

    @Override
    void run() {
        if(!Snapshot.SnapshotDirectory.exists()) {
            if(!Snapshot.SnapshotDirectory.mkdirs()) {
                println "failed to create snapshot directory ${Snapshot.SnapshotDirectory.toString()}"
                return
            }
        }
        Snapshot snapshot = new Snapshot()
        snapshot.file = new File(Snapshot.SnapshotDirectory,"${snapshot.captured.toString()}.snapshot")
        snapshot.tag = tag
        ModuleItem.downloadedModules().each { module ->
            snapshot.addModuleSnapshot(new SnapshotModule(module))
        }
        try {
            snapshot.save()
        } catch(Exception ex) {
            println CommandLine.Help.Ansi.AUTO.string("@|red Unable to create Snapshot: ${ex.getMessage()} |@");
        }
    }
}
