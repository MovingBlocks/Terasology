// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.fixtures;

import org.joml.Vector3i;
import org.terasology.persistence.ChunkStore;
import org.terasology.world.chunks.Chunk;

public class TestChunkStore implements ChunkStore {

    private final Chunk chunk;

    private boolean isEntityRestored;

    public TestChunkStore(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public Vector3i getChunkPosition() {
        return chunk.getPosition(new Vector3i());
    }

    @Override
    public Chunk getChunk() {
        return chunk;
    }

    public boolean isEntityRestored() {
        return isEntityRestored;
    }

    @Override
    public void restoreEntities() {
        isEntityRestored = true;
    }
}
