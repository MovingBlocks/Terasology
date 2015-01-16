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
import java.util.Map;

import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.core.world.generator.facets.FloraFacet;
import org.terasology.core.world.generator.rasterizers.FloraType;
import org.terasology.entitySystem.Component;
import org.terasology.math.TeraMath;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.block.Block;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Determines where plants can be placed.  Will put plants one block above the surface if it is in the correct biome.
 */
@Produces(FloraFacet.class)
@Requires({
    @Facet(SeaLevelFacet.class),
    @Facet(SurfaceHeightFacet.class),
    @Facet(BiomeFacet.class),
    @Facet(value = DensityFacet.class, border = @FacetBorder(bottom = 1))
})
public class FloraProvider implements FacetProvider, ConfigurableFacetProvider {

    private NoiseTable noiseTable;
    private NoiseTable noiseTypeTable;
    private FloraProviderConfiguration configuration = new FloraProviderConfiguration();

    /**
     * Probabilities must add up to 1.0
     */
    private Map<FloraType, Float> probs = ImmutableMap.of(
            FloraType.GRASS, 0.85f,
            FloraType.FLOWER, 0.1f,
            FloraType.MUSHROOM, 0.05f);

    @Override
    public void setSeed(long seed) {
        noiseTable = new NoiseTable(seed);
        noiseTypeTable = new NoiseTable(seed + 1);
    }

    @Override
    public void process(GeneratingRegion region) {
        FloraFacet facet = new FloraFacet(region.getRegion(), region.getBorderForFacet(FloraFacet.class));
        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);
        DensityFacet density = region.getRegionFacet(DensityFacet.class);
        BiomeFacet biomeFacet = region.getRegionFacet(BiomeFacet.class);

        int minY = facet.getWorldRegion().minY();
        int maxY = facet.getWorldRegion().maxY();
        for (int z = facet.getRelativeRegion().minZ(); z <= facet.getRelativeRegion().maxZ(); ++z) {
            for (int x = facet.getRelativeRegion().minX(); x <= facet.getRelativeRegion().maxX(); ++x) {
                int height = TeraMath.floorToInt(surface.get(x, z)) + 1;
                if (height >= minY && height <= maxY && height > seaLevel.getSeaLevel()) {
                    CoreBiome biome = biomeFacet.get(x, z);
                    height = height - minY + facet.getRelativeRegion().minY();

                    float below = density.get(x, height - 1, z);
                    float curr = density.get(x, height, z);
                    if (below >= 0 && curr < 0) {
                        float plantProb = getPlantProb(biome);
                        if (noiseTable.noise(x, z) / 255.0f < plantProb) {
                            FloraType type = getType(x, z);
                            facet.set(x, height, z, type);
                        }
                    }
                }
            }
        }
        region.setRegionFacet(FloraFacet.class, facet);
    }

    protected FloraType getType(int x, int z) {
        float random = noiseTypeTable.noise(x, z) / 255.0f;

        for (FloraType generator : probs.keySet()) {
            float threshold = probs.get(generator).floatValue();
            if (random <= threshold) {
                return generator;
            } else {
                random -= threshold;
            }
        }

        // this should never happen, but if it does: return first element
        return FloraType.values()[0];
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
