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

import org.terasology.entitySystem.Component;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2f;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Updates;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * Applies an amount of the max depth for regions that are oceans
 */
@Updates(@Facet(SurfaceHeightFacet.class))
public class PerlinOceanProvider implements ConfigurableFacetProvider {
    private static final int SAMPLE_RATE = 4;

    private SubSampledNoise oceanNoise;
    private PerlinOceanConfiguration configuration = new PerlinOceanConfiguration();

    @Override
    public void setSeed(long seed) {
        oceanNoise = new SubSampledNoise(new BrownianNoise(new PerlinNoise(seed + 1), 8), new Vector2f(0.0009f, 0.0009f), SAMPLE_RATE);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceHeightFacet facet = region.getRegionFacet(SurfaceHeightFacet.class);
        float[] noise = oceanNoise.noise(facet.getWorldRegion());

        float[] surfaceHeights = facet.getInternal();
        for (int i = 0; i < noise.length; ++i) {
            surfaceHeights[i] -= configuration.maxDepth * TeraMath.clamp(noise[i] * 8.0f * 2.11f + 0.25f);
        }
    }

    @Override
    public String getConfigurationName() {
        return "Oceans";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (PerlinOceanConfiguration) configuration;
    }

    private static class PerlinOceanConfiguration implements Component {
        @Range(min = 0, max = 128f, increment = 1f, precision = 0, description = "Ocean Depth")
        public float maxDepth = 32;
    }
}
