// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.workspace.snapshot

import org.eclipse.jgit.api.Git
import org.terasology.cli.ModuleItem
import org.terasology.cli.Snapshot
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
                if (!item.validModule()) {
                    println CommandLine.Help.Ansi.AUTO.string("@|green Init module ${item.name()}|@")
                    Git.cloneRepository()
                        .setURI(snapshotModule.getRemote())
                        .setDirectory(item.getDirectory())
                        .call()
                    ModuleItem.copyInTemplates(item)

                }

                if (exact) {
                    println CommandLine.Help.Ansi.AUTO.string("@|green Checkout module ${item.name()} commit ${snapshotModule.getCommit()}|@")
                    Git.open(item.getDirectory())
                        .checkout()
                        .setName(snapshotModule.getCommit())
                        .call()

                } else {
                    println CommandLine.Help.Ansi.AUTO.string("@|green Checkout module ${item.name()} branch ${snapshotModule.getBranch()}|@")
                    Git.open(item.getDirectory())
                        .checkout()
                        .setName(snapshotModule.getBranch())
                        .call()
                }

            }
            if (removeModule) {
                Set<ModuleItem> current = snapshot.modules().collect { m -> m.module }
                (ModuleItem.downloadedModules() - current).each { removeModules ->
                    removeModules.getDirectory().delete()
                }
            }

        }, {
            println "failed to resolve snapshot: ${name}"
        })
    }
}
