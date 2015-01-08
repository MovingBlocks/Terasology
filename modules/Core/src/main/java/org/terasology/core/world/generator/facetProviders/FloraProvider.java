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

import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.core.world.generator.facets.PlantFacet;
import org.terasology.entitySystem.Component;
import org.terasology.math.TeraMath;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * Determines where plants can be placed.  Will put plants one block above the surface if it is in the correct biome.
 */
@Produces(PlantFacet.class)
@Requires({
    @Facet(SurfaceHeightFacet.class),
    @Facet(BiomeFacet.class),
    @Facet(value = DensityFacet.class, border = @FacetBorder(bottom = 1))
})
public class FloraProvider implements FacetProvider, ConfigurableFacetProvider {

    private NoiseTable noiseTable;
    private FloraProviderConfiguration configuration = new FloraProviderConfiguration();

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
                int height = TeraMath.ceilToInt(surface.get(x, z));
                if (height >= minY && height <= maxY) {
                    CoreBiome biome = biomeFacet.get(x, z);
                    height = height - minY + facet.getRelativeRegion().minY();

                    float below = density.get(x, height - 1, z);
                    float curr = density.get(x, height, z);
                    if (below >= 0 && curr < 0) {
                        float plantProb = getPlantProb(biome);
                        if (noiseTable.noise(x, z) / 255.0f < plantProb) {
                            facet.set(x, height, z, true);
                        }
                    }
                }
            }
        }
        region.setRegionFacet(PlantFacet.class, facet);
    }

    private float getPlantProb(CoreBiome biome) {
        switch (biome) {
            case DESERT:
                return configuration.desertGrassDensity;

            case FOREST:
                return configuration.forestGrassDensity;

            case MOUNTAINS:
                return configuration.mountainGrassDensity;

            case PLAINS:
                return configuration.plainsGrassDensity;

            case SNOW:
                return configuration.snowGrassDensity;

            default:
                return 0;
        }
    }

    @Override
    public String getConfigurationName() {
        return "Flora";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (FloraProviderConfiguration) configuration;
    }

    private static class FloraProviderConfiguration implements Component {

        @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the grass density for forests")
        private float forestGrassDensity = 0.3f;

        @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the grass density for plains")
        private float plainsGrassDensity = 0.2f;

        @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the grass density for snow")
        private float snowGrassDensity = 0.001f;

        @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the grass density for mountains")
        private float mountainGrassDensity = 0.2f;

        @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the grass density for deserts")
        private float desertGrassDensity = 0.001f;
    }
}
