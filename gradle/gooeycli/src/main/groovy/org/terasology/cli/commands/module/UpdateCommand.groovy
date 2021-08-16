// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands.module

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullResult
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.lib.ObjectId
import org.terasology.cli.ModuleItem
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

@Command(name = "update", description = "Gets one or more items directly")
class UpdateCommand implements Runnable {
    @Parameters(paramLabel = "items", arity = "1..*", description = "Target item(s) to get")
    List<String> items = []

    @Parameters(paramLabel = "force", description = "dismiss all local changes")
    boolean force;

    @Parameters(paramLabel = "force", description = "reset back to the default upstream branch (develop)")
    boolean reset;

    @Override
    void run() {
        for(module in items.collect({it -> new ModuleItem(it)})) {
            Git git = Git.open(module.getDirectory());
            ObjectId oldTree = git.getRepository().resolve("HEAD^{tree}");
            try {
                PullResult result = git.pull().call()

                DiffFormatter df = new DiffFormatter(System.out);
                df.setDiffComparator(RawTextComparator.DEFAULT)
                df.setRepository(git.getRepository())
                df.setBinaryFileThreshold(1000);
                df.setDetectRenames(true)

                if (result.isSuccessful()) {
                    ObjectId newTree = git.getRepository().resolve("HEAD^{tree}");
                    List<DiffEntry> diffs = df.scan(oldTree, newTree);
                    if(diffs.size() > 0) {
                        println CommandLine.Help.Ansi.AUTO.string("@|green Successfully update ${module.name()} |@")
                    } else {
                        println CommandLine.Help.Ansi.AUTO.string("@|yellow Already updatead ${module.name()} - Skipping |@")
                    }
                    for(DiffEntry entry : diffs) {
                        df.format(entry)
                    }
                } else {
                    println CommandLine.Help.Ansi.AUTO.string("@|red unable to update with un-commited changes |@")
                    for(DiffEntry entry :git.diff().call()) {
                        df.format(entry)
                    }
                }
                df.close()
            } catch(Exception ex) {
                println CommandLine.Help.Ansi.AUTO.string("@|red Unable to update ${module.name()}, Skipping: ${ex.getMessage()} |@");
            }
        }
    }
}
