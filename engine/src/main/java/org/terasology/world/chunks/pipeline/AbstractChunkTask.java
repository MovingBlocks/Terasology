// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import org.joml.Vector3i;
import org.terasology.world.chunks.Chunk;

import java.util.concurrent.ForkJoinTask;

/**
 * Abstract chunk task with processing chunk.
 */
public abstract class AbstractChunkTask extends ForkJoinTask<Chunk> implements ChunkTask {
    protected ForkJoinTask<Chunk> chunkFuture;
    private Chunk result;

    public AbstractChunkTask() {
    }

    public AbstractChunkTask(ForkJoinTask<Chunk> chunk) {
        this.chunkFuture = chunk;
    }

    @Override
    public Chunk getRawResult() {
        return result;
    }

    @Override
    protected void setRawResult(Chunk value) {
        result = value;
    }

    @Override
    public Vector3i getPosition() {
        if (chunkFuture instanceof AbstractChunkTask) {
            return ((AbstractChunkTask) chunkFuture).getPosition();
        }
        return ChunkTask.super.getPosition();
    }

    @Override
    public Chunk getChunk() {
        return null;
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
