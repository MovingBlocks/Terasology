// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.generation;

import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.CoreChunk;

import java.util.Set;

/**
 */
public interface World {

    Region getWorldData(BlockRegion region);

    /**
     * @return the sea level, measured in blocks. May be used for setting
     *         such things as the block layer to use in water reflections.
     */
    int getSeaLevel();

    void rasterizeChunk(CoreChunk chunk, EntityBuffer buffer);

    /**
     * @return a <b>new</b> set containing all facet classes
     */
    Set<Class<? extends WorldFacet>> getAllFacets();

    void initialize();
}
