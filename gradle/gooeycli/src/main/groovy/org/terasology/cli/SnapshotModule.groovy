// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository

class SnapshotModule {
    public static String Version = "1.0"
    private ModuleItem module
    private String commit
    private String branch

    /**
     * captures the current snapshot of the module
     * @param module
     */
    SnapshotModule(ModuleItem module) {
        this.module = module
        Repository repository = Git.open(this.module.getDirectory()).getRepository()
        this.commit = repository.getRefDatabase().findRef("HEAD").objectId.getName()
        this.branch = repository.getBranch()
    }

    private SnapshotModule() {

    }

    ModuleItem getModule() {
        return module
    }

    static SnapshotModule decode(Object obj) {
        SnapshotModule snapshot = new SnapshotModule()
        snapshot.module = new ModuleItem(obj.module.name)
        return snapshot
    }

    static Map encode(SnapshotModule snapshot) {
        return [
            module: [
                name: snapshot.module.name()
            ],
            commit: snapshot.commit,
            branch: snapshot.branch
        ]
    }


}
