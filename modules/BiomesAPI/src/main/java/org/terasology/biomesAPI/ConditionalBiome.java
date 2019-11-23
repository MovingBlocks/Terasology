/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.biomesAPI;

import org.terasology.math.geom.BaseVector2i;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.WorldFacet;
import org.terasology.world.generation.facets.base.FieldFacet2D;

import java.util.Set;

/**
 * This interface allows biomes to accept various limits on where they may generate.
 */
public interface ConditionalBiome extends Biome {

    /**
     * Returns true if the biome can generate at the given value of the given facet.
     * @param facetClass The facet to check, such as humidity or temperature.
     * @param value This facet's value.
     * @return True if possible.
     */
    default boolean isValid(Class<FieldFacet2D> facetClass, Float value)
    {
        return true;
    }

    /**
     * Checks whether all facets of the given location meet this biome's restrictions.
     * @param region The game region being generated.
     * @param pos The particular position we are checking.
     * @return True if this biome's conditions are met.
     */
    default boolean isValid(GeneratingRegion region, BaseVector2i pos)
    {
        for (Class<FieldFacet2D> classy : getLimitedFacets())
        {
            FieldFacet2D facetResult = region.getRegionFacet(classy);
            if (!isValid(classy, facetResult.get(pos)))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return A list of all facets that this biome has restrictions towards.
     */
    Set<Class<FieldFacet2D>> getLimitedFacets();

    void setLowerLimit(Class<FieldFacet2D> facetClass, Float minimum);

    void setUpperLimit(Class<FieldFacet2D> facetClass, Float maximum);

    default void setLimits(Class<FieldFacet2D> facetClass, Float minimum, Float maximum)
    {
        setLowerLimit(facetClass, minimum);
        setUpperLimit(facetClass, maximum);
    }

}
