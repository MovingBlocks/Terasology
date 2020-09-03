// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.tasks;

import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.AbstractChunkTask;
import org.terasology.world.propagation.light.LightMerger;

public class LightMergerChunkTask extends AbstractChunkTask {

    private final Chunk[] localChunks;
    private final LightMerger lightMerger;

    public LightMergerChunkTask(Chunk chunk, Chunk[] localChunks, LightMerger lightMerger) {
        super(chunk);
        this.localChunks = localChunks;
        this.lightMerger = lightMerger;
    }

    @Override
    public String getName() {
        return "Light Merging";
    }

    @Override
    public void run() {
        lightMerger.merge(chunk, localChunks);
    }
}
