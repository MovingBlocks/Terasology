// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.tasks;

import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.LitChunk;
import org.terasology.world.chunks.pipeline.AbstractChunkTask;
import org.terasology.world.propagation.light.InternalLightProcessor;

/**
 * Chunk task for run {@link InternalLightProcessor#generateInternalLighting(LitChunk)}
 */
public class GenerateInternalLightningChunkTask extends AbstractChunkTask {

    public GenerateInternalLightningChunkTask(Chunk chunk) {
        super(chunk);
    }

    @Override
    public String getName() {
        return "GenerateInternalLightning";
    }

    @Override
    public void run() {
        InternalLightProcessor.generateInternalLighting(chunk);
    }

}
