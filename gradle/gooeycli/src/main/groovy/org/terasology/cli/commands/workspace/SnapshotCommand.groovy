// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.workspace

import groovy.json.JsonOutput
import org.terasology.cli.ModuleItem
import org.terasology.cli.Snapshot
import org.terasology.cli.SnapshotModule
import picocli.CommandLine.Parameters
import picocli.CommandLine.Command

@Command(name = "snapshot",
    description = "captures a snapshot of all the modules and the associated commit")
class SnapshotCommand implements Runnable {

    @Parameters(paramLabel = "name", description = "tag snapshot with name", defaultValue = "")
    String name;

    @Override
    void run() {
        if(!Snapshot.SnapshotDirectory.exists()) {
            if(!Snapshot.SnapshotDirectory.mkdirs()) {
                println "failed to create snapshot directory ${Snapshot.SnapshotDirectory.toString()}"
                return
            }
        }
        Snapshot snapshot = new Snapshot()
        ModuleItem.downloadedModules().each { module ->
            snapshot.addModuleSnapshot(new SnapshotModule(module))
        }

        File file = new File(Snapshot.SnapshotDirectory,"${snapshot.captureDate.toString()}.snapshot")
        file.write(JsonOutput.toJson(Snapshot.encode(snapshot)))
    }
}
