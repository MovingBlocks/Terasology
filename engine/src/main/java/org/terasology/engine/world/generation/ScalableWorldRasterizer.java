// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.generation;

import org.terasology.engine.world.chunks.Chunk;

public interface ScalableWorldRasterizer extends WorldRasterizer {
    void generateChunk(Chunk chunk, Region chunkRegion, float scale);

    @Override
    default void generateChunk(Chunk chunk, Region chunkRegion) {
        generateChunk(chunk, chunkRegion, 1);
    }
}
