// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.workspace.snapshot

import org.terasology.cli.items.Snapshot
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

import java.awt.*

@Command(name = "open", description = [
        "open snapshot file in your editor",
        "Opens at GUI, if your environment supports it, Opens with \$EDITOR otherwise"])
class OpenCommand implements Runnable {
    @Parameters(paramLabel = "items", arity = "1", description = "tag snapshot with name")
    String name = ""

    @Override
    void run() {
        if (Desktop.desktopSupported && !GraphicsEnvironment.isHeadless()) {
            Desktop desktop = Desktop.getDesktop()
            Snapshot.find(name).ifPresentOrElse({ it ->
                desktop.open(it.file)
            }, {
                println "failed to resolve snapshot: ${name}"
            })
        } else {
            Snapshot.find(name).ifPresentOrElse({ it ->
                String editor = System.getenv('EDITOR')
                if (editor != null) {
                    println "$editor $it.file"
                    ProcessBuilder pb = new ProcessBuilder(editor, it.file.getAbsolutePath())
                    pb.inheritIO().start().waitFor()

                } else {
                    println "Cannot determinate editor. Please set EDITOR to environment variables."
                }
            }, {
                println "failed to resolve snapshot: ${name}"
            })
        }
    }
}
