// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology.tooling

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.storage.file.FileBasedConfig
import org.eclipse.jgit.util.FS
import org.eclipse.jgit.util.SystemReader
import java.io.File

/**
 * Thin JGit entry points - a Kotlin/JGit-native replacement for the Grgit (Groovy) calls
 * common.groovy used to make. Callers needing several operations on the same repo should use
 * [withRepo] once and drive the returned [Git]/its [Git.getRepository] directly, rather than
 * re-opening the repository per operation.
 */
object GitOps {
    class NoRepository(message: String) : Exception(message)

    init {
        // We only ever clone/fetch public repos over https:// - deliberately, so this tool never
        // needs any credentials set up. Some developers' global ~/.gitconfig rewrites all github.com
        // https:// URLs to git@github.com: ("url.*.insteadOf"), which JGit also respects - forcing SSH
        // auth that may not be set up the same way a working native `git`/`ssh` config is. Don't
        // consult the user-level git config at all, so that rewrite (and anything else in it) can't
        // affect us; we don't need any other setting from it for clone/fetch/pull/remote management.
        val delegate = SystemReader.getInstance()
        SystemReader.setInstance(object : SystemReader.Delegate(delegate) {
            override fun openUserConfig(parent: Config?, fs: FS?): FileBasedConfig {
                // A FileBasedConfig over a nonexistent file loads as empty, without complaint.
                val noSuchFile = File.createTempFile("terasology-tooling-empty-gitconfig", null).also { it.delete() }
                return FileBasedConfig(parent, noSuchFile, fs ?: FS.DETECTED)
            }
        })
    }

    fun clone(dir: File, uri: String, remoteName: String = "origin") {
        Git.cloneRepository()
            .setDirectory(dir)
            .setURI(uri)
            .setRemote(remoteName)
            .call()
            .close()
    }

    fun init(dir: File) {
        Git.init().setDirectory(dir).call().close()
    }

    /** Opens [dir] as a git repo for the duration of [block], translating "not a repo" into [NoRepository]. */
    fun <T> withRepo(dir: File, block: (Git) -> T): T {
        val git = try {
            Git.open(dir)
        } catch (e: RepositoryNotFoundException) {
            throw NoRepository("no repository found at $dir")
        }
        return git.use(block)
    }
}
