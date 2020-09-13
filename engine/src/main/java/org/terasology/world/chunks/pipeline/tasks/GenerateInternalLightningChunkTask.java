// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.tasks;

import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.LitChunk;
import org.terasology.world.chunks.pipeline.AbstractChunkTask;
import org.terasology.world.propagation.light.InternalLightProcessor;

import java.util.concurrent.ForkJoinTask;

/**
 * Chunk task for run {@link InternalLightProcessor#generateInternalLighting(LitChunk)}
 */
public class GenerateInternalLightningChunkTask extends AbstractChunkTask {

    public GenerateInternalLightningChunkTask(ForkJoinTask<Chunk> chunk) {
        super(chunk);
    }

    @Override
    public String getName() {
        return "GenerateInternalLightning";
    }

    @Override
    public void run() {
    }


    @Override
    protected boolean exec() {
        setRawResult(chunkFuture.fork().join());
        InternalLightProcessor.generateInternalLighting(getRawResult());
        return true;
    }
}
