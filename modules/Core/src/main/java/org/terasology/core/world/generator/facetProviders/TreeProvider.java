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

import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.entitySystem.Component;
import org.terasology.math.TeraMath;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
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
@Requires({
        @Facet(value = SeaLevelFacet.class, border = @FacetBorder(sides = 10)),
        @Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(bottom = 15, sides = 10 + 1)),
        @Facet(value = DensityFacet.class, border = @FacetBorder(bottom = 15 + 1, sides = 10)),
        @Facet(value = BiomeFacet.class, border = @FacetBorder(bottom = 15, sides = 10))
})
public class TreeProvider implements ConfigurableFacetProvider {

    private NoiseTable treeNoise;
    private NoiseTable treeSeedNoise;
    private TreeProviderConfiguration configuration = new TreeProviderConfiguration();

    @Override
    public void setSeed(long seed) {
        treeNoise = new NoiseTable(seed);
        treeSeedNoise = new NoiseTable(seed + 1);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D borderForTreeFacet = region.getBorderForFacet(TreeFacet.class);
        TreeFacet facet = new TreeFacet(region.getRegion(), borderForTreeFacet.extendBy(0, 15, 10));
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);
        DensityFacet density = region.getRegionFacet(DensityFacet.class);
        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);

        int minY = facet.getWorldRegion().minY();
        int maxY = facet.getWorldRegion().maxY();

        for (int z = facet.getWorldRegion().minZ(); z <= facet.getWorldRegion().maxZ(); z++) {
            for (int x = facet.getWorldRegion().minX(); x <= facet.getWorldRegion().maxX(); x++) {
                int height = TeraMath.ceilToInt(surface.getWorld(x, z));
                // if the surface is in range, and if we are above sea level
                if (height >= minY && height <= maxY) {

                    if (height > seaLevel.getSeaLevel()) {
                        // if the block on the surface is dense enough
                        float densBelow = density.getWorld(x, height - 1, z);
                        float densThis = density.getWorld(x, height, z);
                        if (densBelow >= 0 && densThis < 0
                                // and if there is a level surface in adjacent directions
                                && (TeraMath.ceilToInt(surface.getWorld(x - 1, z)) == height)
                                && (TeraMath.ceilToInt(surface.getWorld(x + 1, z)) == height)
                                && (TeraMath.ceilToInt(surface.getWorld(x, z - 1)) == height)
                                && (TeraMath.ceilToInt(surface.getWorld(x, z + 1)) == height)
                                // and if it selects a % of them
                                && treeNoise.noise(x, z) / 255f < configuration.density) {
                            facet.setWorld(x, height, z, Integer.valueOf(treeSeedNoise.noise(x, z)));
                        }
                    }
                }
            }
        }

        region.setRegionFacet(TreeFacet.class, facet);
    }

    @Override
    public String getConfigurationName() {
        return "Trees";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (TreeProviderConfiguration) configuration;
    }

    private static class TreeProviderConfiguration implements Component {
        @Range(min = 0, max = 0.3f, increment = 0.01f, precision = 2, description = "Define the tree density for forests")
        private float density = 0.12f;

    }
}
