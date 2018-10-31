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

import java.util.Iterator;

import org.terasology.entitySystem.Component;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.Updates;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generation.facets.SurfaceHumidityFacet;
import org.terasology.world.generation.facets.SurfaceTemperatureFacet;

/**
 * Adds surface height for hill and mountain regions. Mountain and hill regions are based off of temperature and humidity.
 */
@Requires({@Facet(SurfaceTemperatureFacet.class), @Facet(SurfaceHumidityFacet.class)})
@Updates(@Facet(SurfaceHeightFacet.class))
public class PerlinHillsAndMountainsProvider implements ConfigurableFacetProvider {

    private SubSampledNoise mountainNoise;
    private SubSampledNoise hillNoise;
    private PerlinHillsAndMountainsProviderConfiguration configuration = new PerlinHillsAndMountainsProviderConfiguration();

    @Override
    public void setSeed(long seed) {
        // TODO: reduce the number of octaves in BrownianNoise
        mountainNoise = new SubSampledNoise(new BrownianNoise(new PerlinNoise(seed + 3)), new Vector2f(0.0002f, 0.0002f), 4);
        hillNoise = new SubSampledNoise(new BrownianNoise(new PerlinNoise(seed + 4)), new Vector2f(0.0008f, 0.0008f), 4);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceHeightFacet facet = region.getRegionFacet(SurfaceHeightFacet.class);

        float[] mountainData = mountainNoise.noise(facet.getWorldRegion());
        float[] hillData = hillNoise.noise(facet.getWorldRegion());
        SurfaceTemperatureFacet temperatureData = region.getRegionFacet(SurfaceTemperatureFacet.class);
        SurfaceHumidityFacet humidityData = region.getRegionFacet(SurfaceHumidityFacet.class);

        float[] heightData = facet.getInternal();
        Iterator<BaseVector2i> positionIterator = facet.getRelativeRegion().contents().iterator();
        for (int i = 0; i < heightData.length; ++i) {
            BaseVector2i pos = positionIterator.next();
            float temp = temperatureData.get(pos);
            float tempHumid = temp * humidityData.get(pos);
            Vector2f distanceToMountainBiome = new Vector2f(temp - 0.25f, tempHumid - 0.35f);
            float mIntens = TeraMath.clamp(1.0f - distanceToMountainBiome.length() * 3.0f);
            float densityMountains = Math.max(mountainData[i] * 2.12f, 0) * mIntens * configuration.mountainAmplitude;
            float densityHills = Math.max(hillData[i] * 2.12f - 0.1f, 0) * (1.0f - mIntens) * configuration.hillAmplitude;

            heightData[i] = heightData[i] + 1024 * densityMountains + 128 * densityHills;
        }
    }

    @Override
    public String getConfigurationName() {
        return "Hills and Mountains";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (PerlinHillsAndMountainsProviderConfiguration) configuration;
    }

    private static class PerlinHillsAndMountainsProviderConfiguration implements Component {

        @Range(min = 0, max = 3f, increment = 0.01f, precision = 2, description = "Mountain Amplitude")
        public float mountainAmplitude = 1f;

        @Range(min = 0, max = 2f, increment = 0.01f, precision = 2, description = "Hill Amplitude")
        public float hillAmplitude = 1f;
    }
}
