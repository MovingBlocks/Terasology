// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository

class SnapshotModule {
    private ModuleItem module
    private String commit
    private String branch
    private String remote;

    /**
     * captures the current snapshot of the module
     * @param module
     */
    SnapshotModule(ModuleItem module) {
        this.module = module
        Repository repository = Git.open(this.module.getDirectory()).getRepository()
        this.commit = repository.getRefDatabase().findRef("HEAD").objectId.getName()
        this.branch = repository.getBranch()
        this.remote = repository.getConfig().getString("remote", "origin", "url")
    }

    SnapshotModule(Object data, int version) {
        switch (version) {
            case 1:
                this.module = new ModuleItem(data.module.name)
                this.commit = data.commit
                this.branch = data.branch
                this.remote = data.remote
                break
        }
    }

    String getCommit() {
        return commit
    }

    String getBranch() {
        return branch
    }

    String getRemote() {
        return remote
    }

    ModuleItem getModule() {
        return module
    }

    Map encode() {
        return [
            module: [
                name: module.name()
            ],
            remote: remote,
            commit: commit,
            branch: branch
        ]
    }
}
