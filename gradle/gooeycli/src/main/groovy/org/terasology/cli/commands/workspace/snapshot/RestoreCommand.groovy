// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.workspace.snapshot


import org.terasology.cli.Snapshot
import org.terasology.cli.module.ModuleItem
import org.terasology.cli.module.Modules
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Command(name = "restore", description = "Restore workspace from snapshot")
class RestoreCommand implements Runnable {

    @Option(names = ["-remove-modules"],
            description = "remove modules that are unmatched from the snapshot")
    boolean removeModule

    @Option(names = ["-exact"],
            description = "match commit exactly from snapshot else will use branch from snapshot")
    boolean exact

    @Parameters(paramLabel = "items", arity = "1", description = "restore snapshot")
    String name

    @Override
    void run() {
        Snapshot.find(name).ifPresentOrElse({ snapshot ->
            snapshot.modules().each { snapshotModule ->
                ModuleItem item = snapshotModule.module
                if (item.remote) {
                    println CommandLine.Help.Ansi.AUTO.string("@|green Fetch module ${item.name}|@")
                    item.clone(snapshotModule.getRemote())
                            .copyInGradleTemplate()
                }

                if (exact) {
                    println CommandLine.Help.Ansi.AUTO.string("@|green Checkout module ${item.name} commit ${snapshotModule.getCommit()}|@")
                    item.checkout(snapshotModule.getCommit())
                } else {
                    println CommandLine.Help.Ansi.AUTO.string("@|green Checkout module ${item.name} branch ${snapshotModule.getBranch()}|@")
                    item.checkout(snapshotModule.getBranch())
                }
            }

            if (removeModule) {
                Set<ModuleItem> current = snapshot.modules().collect { m -> m.module }
                (Modules.downloadedModules() - current).each { removeModules ->
                    removeModules.dir.delete()
                }
            }

        }, {
            println "failed to resolve snapshot: ${name}"
        })
    }
}
