// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import org.terasology.engine.world.chunks.CoreChunk;

/**
 */
public interface WorldRasterizer {
    void initialize();

    void generateChunk(CoreChunk chunk, Region chunkRegion);
}
