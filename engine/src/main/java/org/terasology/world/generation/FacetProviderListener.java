/*
 * Copyright 2020 MovingBlocks
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

import org.terasology.world.generation.facets.SurfacesFacet;
import org.terasology.world.generator.plugin.RegisterFacetListener;
import org.terasology.world.generator.plugin.WorldGeneratorListener;

/**
 * <p>
 * A {@code FacetProviderListener} is a class that wishes to be notified when certain {@link Facet}s are provided
 * to a {@link GeneratingRegion} during chunk generation. It does so because it wants to know about certain
 * details of the chunk that can be used for later decision making by other systems. Such as:
 * <ul>
 *     <li>Wanting to know the {@link SurfacesFacet surface height} to add buildings or other features</li>
 *     <li>Wanting to know where caves are to spawn monsters in those caves periodically</li>
 *     <li>Wanting to know where the beaches (areas of sand near water) are to add buried treasure</li>
 * </ul>
 * </p>
 * <p>
 * Implementing classes should be annotated with {@link RegisterFacetListener}, providing the facets they are interested in.
 * </p>
 */
public interface FacetProviderListener extends WorldGeneratorListener {

    default void initialize() {
        // don't do anything
    }

    /**
     * Notify the listener of a facet provided to a region.
     * <br/>
     * <strong>This call is made from the world generation thread, not the main thread</strong>. As such it should not
     * do any long running processing and should be thread safe. Ideally it should check if the region-facet combination
     * is of interest, and if so notify another system to do any actual processing <em>on a separate thread</em>.
     *
     * @param region A region that is undergoing generation.
     * @param facet The facet that was provided to the region.
     */
    void notify(GeneratingRegion region, WorldFacet facet);
}
