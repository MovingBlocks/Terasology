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
package org.terasology.core.world.generator.perlinFacetProviders;

import org.terasology.core.world.generator.facets.SeaLevelFacet;
import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * Determines where trees can be placed.  Will put trees one block above the surface.
 */
@Produces(TreeFacet.class)
@Requires({@Facet(SeaLevelFacet.class), @Facet(SurfaceHeightFacet.class), @Facet(value = DensityFacet.class, border = @FacetBorder(bottom = 1))})
public class PerlinTreeProvider implements FacetProvider {

    private static float amountOfTrees = 0.07f;
    private NoiseTable noiseTable;

    @Override
    public void setSeed(long seed) {
        noiseTable = new NoiseTable(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        TreeFacet facet = new TreeFacet(region.getRegion(), region.getBorderForFacet(TreeFacet.class));
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);
        DensityFacet density = region.getRegionFacet(DensityFacet.class);
        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);

        int minY = facet.getWorldRegion().minY();
        int maxY = facet.getWorldRegion().maxY();
        for (int z = facet.getRelativeRegion().minZ(); z <= facet.getRelativeRegion().maxZ(); ++z) {
            for (int x = facet.getRelativeRegion().minX(); x <= facet.getRelativeRegion().maxX(); ++x) {
                int height = TeraMath.floorToInt(surface.get(x, z));
                // if the surface is in range, and if we are above sea level
                if (height >= minY && height < maxY && height >= seaLevel.getSeaLevel()) {
                    height = height - minY + facet.getRelativeRegion().minY();

                    // if the block on the surface is dense enough
                    if (density.get(x, height, z) > 0
                            && density.get(x, height + 1, z) <= 0
                            // and if there is a level surface in adjacent directions
                            && (x > facet.getRelativeRegion().minX() && TeraMath.floorToInt(surface.get(x - 1, z)) == height)
                            && (x < facet.getRelativeRegion().maxX() && TeraMath.floorToInt(surface.get(x + 1, z)) == height)
                            && (z > facet.getRelativeRegion().minZ() && TeraMath.floorToInt(surface.get(x, z - 1)) == height)
                            && (z < facet.getRelativeRegion().maxZ() && TeraMath.floorToInt(surface.get(x, z + 1)) == height)
                            // and if it selects a % of them
                            && noiseTable.noise(x, z) / 256f < amountOfTrees) {
                        facet.set(x, height + 1, z, noiseTable.noise(x, z));
                    }
                }
            }
        }
        region.setRegionFacet(TreeFacet.class, facet);
    }
}
