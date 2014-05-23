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
package org.terasology.world.generation.perlin;

import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.BiomeFacet;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.PlantFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * @author Immortius
 */
@Produces(BiomeFacet.class)
@Requires({@Facet(SurfaceHeightFacet.class), @Facet(BiomeFacet.class), @Facet(value = DensityFacet.class, border = @FacetBorder(bottom = 1))})
public class PerlinFloraProvider implements FacetProvider {

    private NoiseTable noiseTable;

    @Override
    public void setSeed(long seed) {
        noiseTable = new NoiseTable(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        PlantFacet facet = new PlantFacet(region.getRegion(), region.getBorderForFacet(PlantFacet.class));
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);
        DensityFacet density = region.getRegionFacet(DensityFacet.class);
        BiomeFacet biomeFacet = region.getRegionFacet(BiomeFacet.class);

        int minY = facet.getWorldRegion().minY();
        int maxY = facet.getWorldRegion().maxY();
        for (int z = facet.getRelativeRegion().minZ(); z <= facet.getRelativeRegion().maxZ(); ++z) {
            for (int x = facet.getRelativeRegion().minX(); x <= facet.getRelativeRegion().maxX(); ++x) {
                int height = TeraMath.floorToInt(surface.get(x, z));
                if (height >= minY && height < maxY) {
                    WorldBiomeProvider.Biome biome = biomeFacet.get(x, z);
                    boolean plains = biome == WorldBiomeProvider.Biome.PLAINS;
                    height = height - minY + facet.getRelativeRegion().minY();

                    if ((biome == WorldBiomeProvider.Biome.FOREST || biome == WorldBiomeProvider.Biome.PLAINS) && density.get(x, height, z) > 0
                            && density.get(x, height + 1, z) <= 0 && noiseTable.noise(x, z) > 180) {
                        facet.set(x, height + 1, z, true);
                    }
                }
            }
        }
        region.setRegionFacet(PlantFacet.class, facet);
    }
}
