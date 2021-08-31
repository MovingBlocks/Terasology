// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.workspace.snapshot

import org.terasology.cli.Snapshot
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

import java.awt.*

@Command(name = "open", description = "open snapshot file")
class OpenCommand implements Runnable {
    @Parameters(paramLabel = "items", arity = "1", description = "tag snapshot with name")
    String name = ""

    @Override
    void run() {
        Desktop desktop = Desktop.getDesktop()
        Snapshot.find(name).ifPresentOrElse({ it ->
            desktop.open(it.file)
        }, {
            println "failed to resolve snapshot: ${name}"
        })

    }
}
