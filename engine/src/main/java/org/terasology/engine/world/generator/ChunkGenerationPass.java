// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.generator;

import org.terasology.engine.world.chunks.Chunk;

import java.util.Map;

public interface ChunkGenerationPass {

    void setWorldSeed(String seed);

    Map<String, String> getInitParameters();

    void setInitParameters(Map<String, String> initParameters);

    /**
     * Generate the local contents of a chunk. This should be purely deterministic from the chunk contents, chunk
     * position and world seed - should not depend on external state or other data.
     *
     * @param chunk
     */
    void generateChunk(Chunk chunk);

}
