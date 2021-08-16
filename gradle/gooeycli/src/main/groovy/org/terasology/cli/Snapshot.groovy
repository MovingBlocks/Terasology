// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli

import org.terasology.cli.util.Constants

class Snapshot {
    public static final String SnapshotFolder = "snapshots"
    public static final File SnapshotDirectory;

    private List<SnapshotModule> modules = []
    Date captureDate = new Date()

    Snapshot() {

    }

    void addModuleSnapshot(SnapshotModule snapshot) {
        modules.add(snapshot)
    }

    static Map encode(Snapshot snapshot){
        return [
            modules: snapshot.modules.collect( {m ->
                SnapshotModule.encode(m)
            })
        ]

    }

    static  {
        SnapshotDirectory = new File(Constants.ConfigurationPath.toFile(), SnapshotFolder)
    }


}
