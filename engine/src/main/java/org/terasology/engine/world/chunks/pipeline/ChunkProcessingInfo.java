// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import org.joml.Vector3ic;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTask;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTaskProvider;

import java.util.List;

public final class ChunkProcessingInfo {

    private final Vector3ic position;

    private Chunk chunk;
    private ChunkTaskProvider chunkTaskProvider;

    private org.terasology.engine.world.chunks.pipeline.stages.ChunkTask chunkTask;

    public ChunkProcessingInfo(Vector3ic position) {
        this.position = position;
    }

    public Vector3ic getPosition() {
        return position;
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

    public ChunkTask getChunkTask() {
        return chunkTask;
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

    ChunkTask makeChunkTask() {
        if (chunkTask == null) {
            chunkTask = chunkTaskProvider.createChunkTask(position);
        }
        return chunkTask;
    }

    void resetTaskState() {
        chunkTask = null;
    }
}
