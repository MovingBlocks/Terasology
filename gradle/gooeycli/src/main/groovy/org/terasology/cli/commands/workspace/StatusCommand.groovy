// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.workspace


import org.fusesource.jansi.Ansi
import org.terasology.cli.commands.BaseCommandType
import org.terasology.cli.module.Modules
import picocli.CommandLine

@CommandLine.Command(name = "status", description = "show current state of workspace")
class StatusCommand extends BaseCommandType implements Runnable {

    static class Row {
        String name
        boolean isClean
        String branch
        String remote

        Row(String name, boolean isClean, String branch, String remote) {
            this.name = name
            this.isClean = isClean
            this.branch = branch
            this.remote = remote
        }
    }

    @Override
    void run() {
        def rows = Modules.downloadedModules().collect { module ->
            module.open().with {
                def repoName = module.name
                def isClean = status().call().clean
                def branch = repository.branch
                def remote = repository.getConfig().getString("remote", "origin", "url")
                new Row(repoName, isClean, branch, remote)
            }
        }

        Ansi ansi = new Ansi()
        rows.each {
            ansi.format("%-30s", it.name)
            if (it.isClean) {
                ansi.fgGreen().render("  clean").reset()
            } else {
                ansi.fgYellow().render("unclean").reset()
            }
            ansi.render("    ")
            ansi.format("%-20s", it.branch)
            ansi.format("%-30s", it.remote)
            ansi.newline()
        }
        println ansi.toString()

    }
}
