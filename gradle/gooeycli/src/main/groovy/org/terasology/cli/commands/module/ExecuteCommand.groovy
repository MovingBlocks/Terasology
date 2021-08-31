// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.module


import org.eclipse.jgit.diff.DiffEntry
import org.terasology.cli.module.ModuleItem
import org.terasology.cli.module.Modules
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.TimeUnit

@Command(name = "cmd", description = "execute command against all module")
class ExecuteCommand implements Runnable {

    @Option(names = ["-only-modified"], description = "only execute the command on modules that were modified")
    boolean modified

    @Parameters(paramLabel = "cmd", arity = "0", description = "execute system command within module")
    String cmd

    @Parameters(paramLabel = "items", arity = "1..*", description = "Target item(s) to execute against")
    List<String> modules = []

    @Override
    void run() {
        List<ModuleItem> targetModules
        if (modules.size() > 0) {
            targetModules = Modules.resolveModules(modules)
        } else {
            targetModules = Modules.downloadedModules()
        }

        targetModules.each { it ->
            if (it.remote) {
                println CommandLine.Help.Ansi.AUTO.string("@|yellow Module not downloaded ${it.name} - skipping|@")
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
