/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.generation;

import org.terasology.math.Region3i;
import org.terasology.world.chunks.CoreChunk;

import java.util.Map;
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
