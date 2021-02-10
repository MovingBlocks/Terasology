// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.generator;

import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.EntityBuffer;

public interface ScalableWorldGenerator extends WorldGenerator {
    /**
     * Generates all contents of given chunk
     * @param chunk Chunk to generate
     * @param buffer Buffer to queue entities to spawn to
     * @param scale The scale to generate at (larger numbers make the world's features smaller)
     */
    void createChunk(CoreChunk chunk, EntityBuffer buffer, float scale);
}
