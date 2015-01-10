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
package org.terasology.core.world.generator.facetProviders;

import java.util.List;

import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Determines where trees can be placed.  Will put trees one block above the surface.
 */
@Produces(TreeFacet.class)
@Requires(@Facet(SurfaceHeightFacet.class))
public abstract class AbstractTreeProvider implements FacetProvider {

    private NoiseTable treeSeedNoise;

    @Override
    public void setSeed(long seed) {
        treeSeedNoise = new NoiseTable(seed + 1);
    }

    protected TreeFacet createFacet(GeneratingRegion region, List<Predicate<Vector3i>> filters) {
        Border3D borderForTreeFacet = region.getBorderForFacet(TreeFacet.class);
        TreeFacet facet = new TreeFacet(region.getRegion(), borderForTreeFacet.extendBy(0, 15, 10));
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);

        int minY = facet.getWorldRegion().minY();
        int maxY = facet.getWorldRegion().maxY();

        for (int z = facet.getWorldRegion().minZ(); z <= facet.getWorldRegion().maxZ(); z++) {
            for (int x = facet.getWorldRegion().minX(); x <= facet.getWorldRegion().maxX(); x++) {
                int height = TeraMath.ceilToInt(surface.getWorld(x, z));

                // if the surface is in range, check filters
                if (height >= minY && height <= maxY) {

                    Vector3i pos = new Vector3i(x, height, z);

                    if (Predicates.and(filters).apply(pos)) {
                        facet.setWorld(x, height, z, Integer.valueOf(treeSeedNoise.noise(x, z)));
                    }
                }
            }
        }

        return facet;
    }
}

