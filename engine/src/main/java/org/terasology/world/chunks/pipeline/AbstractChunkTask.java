// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import org.terasology.world.chunks.Chunk;

/**
 * Abstract chunk task with processing chunk.
 */
public abstract class AbstractChunkTask implements ChunkTask {
    protected Chunk chunk;

    public AbstractChunkTask() {
    }

    public AbstractChunkTask(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public boolean isTerminateSignal() {
        return false;
    }

    @Override
    public String toString() {
        return "ChunkTask{" + "name = " + getName() + "," +
                "position = " + getPosition() +
                '}';
    }
}
