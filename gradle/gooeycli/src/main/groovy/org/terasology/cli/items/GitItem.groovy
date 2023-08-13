// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.items

import groovy.transform.CompileStatic
import org.eclipse.jgit.api.CloneCommand.Callback
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullResult
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.lib.ObjectId
import picocli.CommandLine

@CompileStatic
trait GitItem<T> {

    abstract String getName()
    abstract File getDir()

    boolean isRemote() {
        !dir.exists()
    }

    T clone(String url, Callback callback = null) {
        if (remote) {
            Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(dir)
                    .setCallback(callback)
                    .call()
        } else {
            //TODO log error
        }
        return this as T
    }

    Git open() {
        if (remote) {

            // TODO log error
        } else {
            return Git.open(dir)
        }
    }


    List<DiffEntry> diff() {
        open().withCloseable {
            it.diff().call()
        }
    }

    T checkout(String branchOrCommit) {
        open().withCloseable {
            it.checkout()
                    .setName(branchOrCommit)
                    .call()
        }
        return this as T
    }

    T update() {
        open().withCloseable { git ->
            ObjectId oldTree = git.getRepository().resolve("HEAD^{tree}")
            try {
                PullResult result = git.pull().call()

                new DiffFormatter(System.out)
                        .withCloseable { df ->
                            df.setDiffComparator(RawTextComparator.DEFAULT)
                            df.setRepository(git.getRepository())
                            df.setBinaryFileThreshold(1000)
                            df.setDetectRenames(true)

                            if (result.isSuccessful()) {
                                ObjectId newTree = git.getRepository().resolve("HEAD^{tree}")
                                List<DiffEntry> diffs = df.scan(oldTree, newTree)
                                if (diffs.size() > 0) {
                                    println CommandLine.Help.Ansi.AUTO.string("@|green Successfully update ${name} |@")
                                } else {
                                    println CommandLine.Help.Ansi.AUTO.string("@|yellow Already updatead ${name} - Skipping |@")
                                }
                                for (DiffEntry entry : diffs) {
                                    df.format(entry)
                                }
                            } else {
                                println CommandLine.Help.Ansi.AUTO.string("@|red unable to update with un-commited changes |@")
                                for (DiffEntry entry : git.diff().call()) {
                                    df.format(entry)
                                }
                            }
                        }
            } catch (Exception ex) {
                println CommandLine.Help.Ansi.AUTO.string("@|red Unable to update ${name}, Skipping: ${ex.getMessage()} |@")
            }
        }
        return this as T
    }
}
