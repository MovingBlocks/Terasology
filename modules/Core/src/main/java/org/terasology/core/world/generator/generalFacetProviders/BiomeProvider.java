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
package org.terasology.core.world.generator.generalFacetProviders;

import org.terasology.math.Vector2i;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.BiomeFacet;
import org.terasology.world.generation.facets.HumidityFacet;
import org.terasology.world.generation.facets.SeaLevelTemperatureFacet;

/**
 * Determines the biome based on temperature and humidity
 */
@Produces(BiomeFacet.class)
@Requires({@Facet(SeaLevelTemperatureFacet.class), @Facet(HumidityFacet.class)})
public class BiomeProvider implements FacetProvider {

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {
        SeaLevelTemperatureFacet temperature = region.getRegionFacet(SeaLevelTemperatureFacet.class);
        HumidityFacet humidityFacet = region.getRegionFacet(HumidityFacet.class);

        Border3D border = region.getBorderForFacet(BiomeFacet.class);
        BiomeFacet biomeFacet = new BiomeFacet(region.getRegion(), border);
        for (Vector2i pos : biomeFacet.getRelativeRegion()) {
            float temp = temperature.get(pos);
            float hum = temp * humidityFacet.get(pos);
            if (temp >= 0.5f && hum < 0.3f) {
                biomeFacet.set(pos, WorldBiomeProvider.Biome.DESERT);
            } else if (hum >= 0.3f && hum <= 0.6f && temp >= 0.5f) {
                biomeFacet.set(pos, WorldBiomeProvider.Biome.PLAINS);
            } else if (temp <= 0.3f && hum > 0.5f) {
                biomeFacet.set(pos, WorldBiomeProvider.Biome.SNOW);
            } else if (hum >= 0.2f && hum <= 0.6f && temp < 0.5f) {
                biomeFacet.set(pos, WorldBiomeProvider.Biome.MOUNTAINS);
            } else {
                biomeFacet.set(pos, WorldBiomeProvider.Biome.FOREST);
            }
        }
        region.setRegionFacet(BiomeFacet.class, biomeFacet);
    }
}
