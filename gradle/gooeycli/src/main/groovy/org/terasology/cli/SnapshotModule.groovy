// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli


import org.terasology.cli.items.ModuleItem

class SnapshotModule {
    private ModuleItem module
    private String commit
    private String branch
    private String remote

    /**
     * captures the current snapshot of the module
     * @param module
     */
    SnapshotModule(ModuleItem module) {
        this.module = module
        module.open().withCloseable {
            this.commit = it.getRepository().getRefDatabase().findRef("HEAD").objectId.getName()
            this.branch = it.getRepository().getBranch()
            this.remote = it.getRepository().getConfig().getString("remote", "origin", "url")
        }
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
                        name: module.name
                ],
                remote: remote,
                commit: commit,
                branch: branch
        ]
    }
}
