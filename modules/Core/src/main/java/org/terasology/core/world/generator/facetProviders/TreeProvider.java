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

import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * Determines where trees can be placed.  Will put trees one block above the surface.
 */
@Produces(TreeFacet.class)
@Requires({@Facet(SeaLevelFacet.class),
        @Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(bottom = 15, sides = 10)),
        @Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(bottom = 15, sides = 10)),
        @Facet(value = DensityFacet.class, border = @FacetBorder(bottom = 15, sides = 10))})
public class TreeProvider implements FacetProvider {

    private static float amountOfTrees = 0.12f;
    private NoiseTable treeNoise;
    private NoiseTable treeSeedNoise;

    @Override
    public void setSeed(long seed) {
        treeNoise = new NoiseTable(seed);
        treeSeedNoise = new NoiseTable(seed + 1);
    }

    @Override
    public void process(GeneratingRegion region) {
        TreeFacet facet = new TreeFacet(region.getRegion(), region.getBorderForFacet(TreeFacet.class));
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);
        DensityFacet density = region.getRegionFacet(DensityFacet.class);
        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);

        Rect2i worldRegion2D = Rect2i.createFromMinAndMax(facet.getWorldRegion().minX(),
                facet.getWorldRegion().minZ(),
                facet.getWorldRegion().maxX(),
                facet.getWorldRegion().maxZ());

        for (Vector2i pos : worldRegion2D) {
            int x = pos.getX();
            int z = pos.getY();
            int height = TeraMath.floorToInt(surface.getWorld(x, z));
            // if the surface is in range, and if we are above sea level
            if (facet.getWorldRegion().encompasses(x, height, z) && facet.getWorldRegion().encompasses(x, height + 1, z) && height >= seaLevel.getSeaLevel()) {

                // if the block on the surface is dense enough
                if (density.getWorld(x, height, z) >= 0
                        && density.getWorld(x, height + 1, z) < 0
                        // and if there is a level surface in adjacent directions
                        && (x > facet.getWorldRegion().minX() && TeraMath.floorToInt(surface.getWorld(x - 1, z)) == height)
                        && (x < facet.getWorldRegion().maxX() && TeraMath.floorToInt(surface.getWorld(x + 1, z)) == height)
                        && (z > facet.getWorldRegion().minZ() && TeraMath.floorToInt(surface.getWorld(x, z - 1)) == height)
                        && (z < facet.getWorldRegion().maxZ() && TeraMath.floorToInt(surface.getWorld(x, z + 1)) == height)
                        // and if it selects a % of them
                        && treeNoise.noise(x, z) / 256f < amountOfTrees) {
                    facet.setWorld(x, height + 1, z, treeSeedNoise.noise(x, z));
                }
            }
        }

        region.setRegionFacet(TreeFacet.class, facet);
    }
}
