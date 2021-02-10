// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import com.google.common.util.concurrent.SettableFuture;
import org.joml.Vector3ic;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.stages.ChunkTask;
import org.terasology.world.chunks.pipeline.stages.ChunkTaskProvider;

import java.util.List;
import java.util.concurrent.Future;

public final class ChunkProcessingInfo {
    private final Vector3ic position;
    private final SettableFuture<Chunk> externalFuture;

    private Chunk chunk;
    private ChunkTaskProvider chunkTaskProvider;

    private Future<Chunk> currentFuture;
    private org.terasology.world.chunks.pipeline.stages.ChunkTask chunkTask;

    public ChunkProcessingInfo(Vector3ic position, SettableFuture<Chunk> externalFuture) {
        this.position = position;
        this.externalFuture = externalFuture;
    }

    public Vector3ic getPosition() {
        return position;
    }

    public SettableFuture<Chunk> getExternalFuture() {
        return externalFuture;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public ChunkTaskProvider getChunkTaskProvider() {
        return chunkTaskProvider;
    }

    public void setChunkTaskProvider(ChunkTaskProvider chunkTaskProvider) {
        this.chunkTaskProvider = chunkTaskProvider;
    }

    public Future<Chunk> getCurrentFuture() {
        return currentFuture;
    }

    public void setCurrentFuture(Future<Chunk> currentFuture) {
        this.currentFuture = currentFuture;
    }

    public org.terasology.world.chunks.pipeline.stages.ChunkTask getChunkTask() {
        return chunkTask;
    }

    public void setChunkTask(org.terasology.world.chunks.pipeline.stages.ChunkTask chunkTask) {
        this.chunkTask = chunkTask;
    }

    boolean hasNextStage(List<ChunkTaskProvider> stages) {
        if (chunkTaskProvider == null) {
            return true;
        } else {
            return stages.indexOf(chunkTaskProvider) != stages.size() - 1;
        }
    }

    void nextStage(List<ChunkTaskProvider> stages) {
        int nextStageIndex =
                chunkTaskProvider == null
                        ? 0
                        : stages.indexOf(chunkTaskProvider) + 1;
        chunkTaskProvider = stages.get(nextStageIndex);
    }

    void endProcessing() {
        externalFuture.set(chunk);
    }

    ChunkTask makeChunkTask() {
        if (chunkTask == null) {
            chunkTask = chunkTaskProvider.createChunkTask(position);
        }
        return chunkTask;
    }

    void resetTaskState() {
        currentFuture = null;
        chunkTask = null;
    }
}
