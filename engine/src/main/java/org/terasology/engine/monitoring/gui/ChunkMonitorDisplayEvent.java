// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.gui;

import com.google.common.base.Preconditions;
import org.joml.Vector3i;
import org.terasology.engine.monitoring.chunk.ChunkMonitorEntry;

public abstract class ChunkMonitorDisplayEvent {

    public final ChunkMonitorDisplay display;

    public ChunkMonitorDisplayEvent(ChunkMonitorDisplay display) {
        this.display = Preconditions.checkNotNull(display, "The parameter 'display' must not be null");
    }

    public abstract static class EntryEvent extends ChunkMonitorDisplayEvent {

        public final ChunkMonitorEntry entry;
        public final Vector3i pos;

        public EntryEvent(ChunkMonitorDisplay display, Vector3i pos, ChunkMonitorEntry entry) {
            super(display);
            this.entry = entry;
            this.pos = pos == null ? null : new Vector3i(pos);
        }
    }

    public static class Selected extends EntryEvent {

        public Selected(ChunkMonitorDisplay display, Vector3i pos, ChunkMonitorEntry entry) {
            super(display, pos, entry);
        }
    }
}
