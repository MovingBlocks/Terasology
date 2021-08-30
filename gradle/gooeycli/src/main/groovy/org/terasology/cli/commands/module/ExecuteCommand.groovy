// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.module

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.terasology.cli.ModuleItem
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.TimeUnit

@Command(name = "cmd", description = "execute command against all module")
class ExecuteCommand implements Runnable{

    @Option(names = ["-only-modified"], description = "only execute the command on modules that were modified")
    boolean modified

    @Parameters(paramLabel = "cmd", arity = "0", description = "execute system command within module")
    String cmd

    @Parameters(paramLabel = "items", arity = "1..*", description = "Target item(s) to execute against")
    List<String> modules = []

    @Override
    void run() {
        List<ModuleItem> targetModules = []
        if(modules.size() > 0) {
            targetModules = modules.collect({it -> new ModuleItem(it)})
        } else {
            targetModules = ModuleItem.downloadedModules()
        }

        targetModules.each { it ->
            if(!it.validModule()) {
                println CommandLine.Help.Ansi.AUTO.string("@|yellow Module not downloaded ${it.name()} - skipping|@")
                return
            }

            if(modified) {
                List<DiffEntry> result = Git.open(it.getDirectory())
                    .diff().call()
                if(result.size() == 0) {
                    return
                }
            }

            println "'${cmd}' executed in ${it.getDirectory().toString()}"
            Process pr = cmd.execute([], it.directory)
            while (!pr.waitFor(1, TimeUnit.SECONDS)) {
                pr.getInputStream().transferTo(System.out)
                pr.getErrorStream().transferTo(System.out)
            }
            pr.getInputStream().transferTo(System.out)
            pr.getErrorStream().transferTo(System.out)
            println "exited with ${pr.waitFor()}"
        }
    }
}
