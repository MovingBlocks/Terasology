// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.Chunk;

import java.util.Set;

/**
 */
public interface World {

    Region getWorldData(BlockRegion region, float scale);

    default Region getWorldData(BlockRegion region) {
        return getWorldData(region, 1);
    }

    /**
     * @return the sea level, measured in blocks. May be used for setting
     *         such things as the block layer to use in water reflections.
     */
    int getSeaLevel();

    void rasterizeChunk(Chunk chunk, EntityBuffer buffer);

    void rasterizeChunk(Chunk chunk, float scale);

    /**
     * @return a <b>new</b> set containing all facet classes
     */
    Set<Class<? extends WorldFacet>> getAllFacets();

    void initialize();
}
