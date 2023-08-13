// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.items

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.terasology.cli.config.Config

import java.time.Instant
import java.time.temporal.ChronoUnit

class Snapshot {
    public static final int Version = 1
    public static final String SnapshotFolder = "snapshots"
    public static final File SnapshotDirectory

    int index = 0
    private List<SnapshotModule> modules = []
    File file
    Instant captured = Instant.now().truncatedTo(ChronoUnit.SECONDS)
    String tag = ""

    Snapshot(File target) {
        this.file = target
        if (this.file.exists()) {
            JsonSlurper slurper = new JsonSlurper()
            def data = slurper.parse(this.file)
            int version = data.version
            switch (version) {
                case 1:
                    this.captured = Instant.parse(data.captured)
                    this.modules.addAll(data.modules.collect({ m -> new SnapshotModule(m, version) }))
                    this.tag = data.tag
                    this.index = data.index
                    break
            }
        }
    }

    Snapshot() {
    }

    void addModuleSnapshot(SnapshotModule snapshot) {
        modules.add(snapshot)
    }

    List<SnapshotModule> modules() {
        return this.modules
    }

    void save() {
        this.file.write(JsonOutput.toJson([
                version : Version,
                tag     : this.tag,
                index   : this.index,
                captured: this.captured.toString(),
                modules : this.modules.collect({ m -> m.encode() })
        ]))
    }

    static Optional<Snapshot> find(String name) {
        List<Snapshot> snapshots = currentSnapshots()
        File target = new File(name)
        if (target.exists()) {
            return Optional.of(new Snapshot(target))
        }

        try {
            int index = Integer.parseInt(name)
            if (index < snapshots.size() && index >= 0) {
                return Optional.of(snapshots[index])
            }
        } catch (NumberFormatException ex) {
            // skip
        }
        for (Snapshot snap : snapshots) {
            if (!snap.getTag()?.trim() && snap.getTag()?.trim() == name.trim()) {
                return Optional.of(snap)
            }
        }

        return Optional.empty()

    }

    static List<Snapshot> currentSnapshots() {
        List<Snapshot> result = []

        SnapshotDirectory.eachFile({ file ->
            result << new Snapshot(file)
        })
        return result.sort({ snap -> snap.captured })
    }

    static {
        SnapshotDirectory = new File(Config.ConfigurationPath.toFile(), SnapshotFolder)
    }
}
