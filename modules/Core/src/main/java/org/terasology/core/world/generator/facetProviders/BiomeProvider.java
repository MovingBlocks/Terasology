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

import org.terasology.core.world.Biome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.math.Vector2i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfaceHumidityFacet;
import org.terasology.world.generation.facets.SurfaceTemperatureFacet;

/**
 * Determines the biome based on temperature and humidity
 */
@Produces(BiomeFacet.class)
@Requires({@Facet(SurfaceTemperatureFacet.class), @Facet(SurfaceHumidityFacet.class)})
public class BiomeProvider implements FacetProvider {

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceTemperatureFacet temperature = region.getRegionFacet(SurfaceTemperatureFacet.class);
        SurfaceHumidityFacet surfaceHumidityFacet = region.getRegionFacet(SurfaceHumidityFacet.class);

        Border3D border = region.getBorderForFacet(BiomeFacet.class);
        BiomeFacet biomeFacet = new BiomeFacet(region.getRegion(), border);
        for (Vector2i pos : biomeFacet.getRelativeRegion()) {
            float temp = temperature.get(pos);
            float hum = temp * surfaceHumidityFacet.get(pos);
            if (temp >= 0.5f && hum < 0.3f) {
                biomeFacet.set(pos, Biome.DESERT);
            } else if (hum >= 0.3f && hum <= 0.6f && temp >= 0.5f) {
                biomeFacet.set(pos, Biome.PLAINS);
            } else if (temp <= 0.3f && hum > 0.5f) {
                biomeFacet.set(pos, Biome.SNOW);
            } else if (hum >= 0.2f && hum <= 0.6f && temp < 0.5f) {
                biomeFacet.set(pos, Biome.MOUNTAINS);
            } else {
                biomeFacet.set(pos, Biome.FOREST);
            }
        }
        region.setRegionFacet(BiomeFacet.class, biomeFacet);
    }
}
