// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.tasks;

import org.joml.Vector3i;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.ChunkTask;

/**
 * Chunk task which interrupt processing in {@link org.terasology.world.chunks.pipeline.ChunkProcessingPipeline}
 * <p>
 * [Warn] this task not cleanup left phases for chunks. Use it with attention.
 * <p>
 * If you not send chunk to processing - you not processed chunk to end -- never.
 */
public class InterruptChunkTask implements ChunkTask {
    @Override
    public Chunk getChunk() {
        return null;
    }

    @Override
    public Vector3i getPosition() {
        return new Vector3i();
    }

    @Override
    public String getName() {
        return "Interrupt";
    }

    @Override
    public void run() {

    }

    @Override
    public boolean isTerminateSignal() {
        return false;
    }
}
