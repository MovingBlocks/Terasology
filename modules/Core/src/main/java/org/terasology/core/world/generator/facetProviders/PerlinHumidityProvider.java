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
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.facets.SurfaceHumidityFacet;

/**
 * Defines surface humidity in the range [0..1] based on random noise.
 */
@Produces(SurfaceHumidityFacet.class)
public class PerlinHumidityProvider implements ConfigurableFacetProvider {
    private static final int SAMPLE_RATE = 4;

    private SubSampledNoise humidityNoise;

    private Configuration config = new Configuration();

    private long seed;

    public PerlinHumidityProvider() {
        // use default values
    }

    /**
     * @param config the config to use
     */
    public PerlinHumidityProvider(Configuration config) {
        this.config = config;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        reload();
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(SurfaceHumidityFacet.class);
        SurfaceHumidityFacet facet = new SurfaceHumidityFacet(region.getRegion(), border);

        float[] noise = humidityNoise.noise(facet.getWorldRegion());
        for (int i = 0; i < noise.length; ++i) {
            noise[i] = TeraMath.clamp((noise[i] * 2.11f + 1f) * 0.5f);
        }
        facet.set(noise);
        region.setRegionFacet(SurfaceHumidityFacet.class, facet);
    }

    @Override
    public String getConfigurationName() {
        return "Humidity";
    }

    @Override
    public Component getConfiguration() {
        return config;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.config = (Configuration) configuration;
        reload();
    }

    private void reload() {
        float realScale = config.scale * 0.01f;
        Vector2f scale = new Vector2f(realScale, realScale);
        BrownianNoise brown = new BrownianNoise(new PerlinNoise(seed + 6), config.octaves);
        humidityNoise = new SubSampledNoise(brown, scale, SAMPLE_RATE);
    }

    public static class Configuration implements Component {
        @Range(min = 0, max = 10.0f, increment = 1f, precision = 0, description = "The number of noise octaves")
        public int octaves = 8;

        @Range(min = 0.01f, max = 5f, increment = 0.01f, precision = 2, description = "The noise scale")
        public float scale = 0.05f;
    }
}
