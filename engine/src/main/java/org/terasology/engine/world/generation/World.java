// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.chunks.CoreChunk;

import java.util.Set;

/**
 */
public interface World {

    Region getWorldData(Region3i region);

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
