// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.localChunkProvider;

import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.engine.world.chunks.internal.ReadyChunkInfo;
import org.terasology.engine.world.propagation.light.LightMerger;

import java.util.List;

class LightMergingChunkFinalizer implements ChunkFinalizer {

    private LightMerger<ReadyChunkInfo> lightMerger;

    @Override
    public void initialize(final GeneratingChunkProvider generatingChunkProvider) {
        lightMerger = new LightMerger<>(generatingChunkProvider);
    }

    @Override
    public List<ReadyChunkInfo> completeFinalization() {
        return lightMerger.completeMerge();
    }

    @Override
    public void beginFinalization(final Chunk chunk, final ReadyChunkInfo readyChunkInfo) {
        lightMerger.beginMerge(chunk, readyChunkInfo);
    }

    @Override
    public void restart() {
        lightMerger.restart();
    }

    @Override
    public void shutdown() {
        lightMerger.shutdown();
    }
}
