/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.monitoring.gui;

import com.google.common.base.Preconditions;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.chunk.ChunkMonitorEntry;

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
