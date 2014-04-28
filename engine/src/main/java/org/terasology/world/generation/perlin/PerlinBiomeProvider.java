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

import org.terasology.math.Vector2i;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.BiomeFacet;
import org.terasology.world.generation.facets.HumidityFacet;
import org.terasology.world.generation.facets.SeaLevelTemperatureFacet;

/**
 * @author Immortius
 */
@Produces(BiomeFacet.class)
@Requires({SeaLevelTemperatureFacet.class, HumidityFacet.class})
public class PerlinBiomeProvider implements FacetProvider {

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {
        SeaLevelTemperatureFacet temperature = region.getRegionFacet(SeaLevelTemperatureFacet.class);
        HumidityFacet humidityFacet = region.getRegionFacet(HumidityFacet.class);
        float[] tempInternal = temperature.getInternal();
        float[] humidityInternal = humidityFacet.getInternal();

        BiomeFacet biomeFacet = new BiomeFacet(new Vector2i(region.getRegion().sizeX(), region.getRegion().sizeZ()));
        WorldBiomeProvider.Biome[] biomeInternal = biomeFacet.getInternal();
        for (int i = 0; i < biomeInternal.length; ++i) {
            float temp = tempInternal[i];
            float hum = temp * humidityInternal[i];
            if (temp >= 0.5f && hum < 0.3f) {
                biomeInternal[i] = WorldBiomeProvider.Biome.DESERT;
            } else if (hum >= 0.3f && hum <= 0.6f && temp >= 0.5f) {
                biomeInternal[i] = WorldBiomeProvider.Biome.PLAINS;
            } else if (temp <= 0.3f && hum > 0.5f) {
                biomeInternal[i] = WorldBiomeProvider.Biome.SNOW;
            } else if (hum >= 0.2f && hum <= 0.6f && temp < 0.5f) {
                biomeInternal[i] = WorldBiomeProvider.Biome.MOUNTAINS;
            } else {
                biomeInternal[i] = WorldBiomeProvider.Biome.FOREST;
            }
        }
        region.setRegionFacet(BiomeFacet.class, biomeFacet);
    }
}
