// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.chunk;

import com.google.common.base.Preconditions;
import org.joml.Vector3i;
import org.terasology.engine.world.chunks.Chunk;

import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

public class ChunkMonitorEntry {

    private final Vector3i pos;
    private Deque<WeakReference<Chunk>> chunks = new LinkedList<>();

    public ChunkMonitorEntry(Vector3i pos) {
        this.pos = Preconditions.checkNotNull(pos, "The parameter 'pos' must not be null");
    }

    private void purge() {
        if (chunks.size() == 0) {
            return;
        }
        final Iterator<WeakReference<Chunk>> it = chunks.iterator();
        while (it.hasNext()) {
            final WeakReference<Chunk> w = it.next();
            if (w.get() == null) {
                it.remove();
            }
        }
    }

    public Vector3i getPosition() {
        return new Vector3i(pos);
    }

    public Chunk getLatestChunk() {
        final WeakReference<Chunk> chunk = chunks.peekLast();
        if (chunk != null) {
            return chunk.get();
        }
        return null;
    }

    public void addChunk(Chunk value) {
        Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
        Preconditions.checkArgument(pos.equals(value.getPosition(new Vector3i())),
                "Expected chunk for position {} but got position {} instead", pos, value.getPosition(new Vector3i()));
        purge();
        chunks.add(new WeakReference<>(value));
    }
}
