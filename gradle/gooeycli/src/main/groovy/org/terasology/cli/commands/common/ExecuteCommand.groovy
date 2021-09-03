// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.common

import org.eclipse.jgit.diff.DiffEntry
import org.terasology.cli.commands.items.ItemCommand
import org.terasology.cli.items.GitItem
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.TimeUnit


 // TODO rework to more unix style ( ./gw  (module|meta|..) cmd items... -- cmd
@Command(name = "cmd", description = "execute command against all item")
class ExecuteCommand implements Runnable {

    @CommandLine.ParentCommand
    ItemCommand<GitItem> parent

    @Option(names = ["-only-modified"], description = "only execute the command on modules that were modified")
    boolean modified

    @Parameters(paramLabel = "cmd", arity = "1", description = "execute system command within module")
    String cmd

    @Parameters(paramLabel = "items", arity = "1..*", description = "Target item(s) to execute against")
    List<String> items = []

    @Override
    void run() {
        List<GitItem> targetItem
        if (items.size() > 0) {
            targetItem = items.collect {parent.create(it)}
        } else {
            targetItem = parent.listLocal()
        }

        println targetItem[1].dump()

        targetItem.each { it->
            if (it.remote) {
                println CommandLine.Help.Ansi.AUTO.string("@|yellow Item not downloaded ${it.name} - skipping|@")
                return
            }

            if (modified) {
                List<DiffEntry> result = it.diff()
                if (result.size() == 0) {
                    return
                }
            }

            println "'${cmd}' executed in ${it.dir.toString()}"
            Process process = cmd.execute([], it.dir)
            while (!process.waitFor(1, TimeUnit.SECONDS)) {
                process.getInputStream().transferTo(System.out)
                process.getErrorStream().transferTo(System.out)
            }
            process.getInputStream().transferTo(System.out)
            process.getErrorStream().transferTo(System.out)
            println "exited with ${process.waitFor()}"
        }
    }
}
