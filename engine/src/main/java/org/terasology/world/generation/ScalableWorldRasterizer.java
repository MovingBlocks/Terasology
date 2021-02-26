// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.generation;

import org.terasology.world.chunks.CoreChunk;

public interface ScalableWorldRasterizer extends WorldRasterizer {
    void generateChunk(CoreChunk chunk, Region chunkRegion, float scale);

    @Override
    default void generateChunk(CoreChunk chunk, Region chunkRegion) {
        generateChunk(chunk, chunkRegion, 1);
    }
}
