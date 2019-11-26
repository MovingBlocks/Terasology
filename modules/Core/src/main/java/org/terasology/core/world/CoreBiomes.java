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
package org.terasology.core.world;

import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.world.generation.facets.SurfaceHumidityFacet;
import org.terasology.world.generation.facets.SurfaceTemperatureFacet;

import java.util.stream.Stream;

/**
 * Registers all core biomes with the engine.
 */
@RegisterSystem
public class CoreBiomes extends BaseComponentSystem {
    @In
    private BiomeRegistry biomeRegistry;

    /**
     * Registration of systems must be done in preBegin to be early enough.
     */
    @Override
    public void preBegin() {
        CoreBiome.DESERT.setLowerLimit(SurfaceTemperatureFacet.class, 0.5f);
        CoreBiome.DESERT.setUpperLimit(SurfaceHumidityFacet.class, 0.3f);
        CoreBiome.SNOW.setUpperLimit(SurfaceTemperatureFacet.class, 0.3f);
        CoreBiome.SNOW.setLowerLimit(SurfaceHumidityFacet.class, 0.5f);
        CoreBiome.PLAINS.setLimits(SurfaceHumidityFacet.class, 0.3f, 0.6f);
        CoreBiome.PLAINS.setLowerLimit(SurfaceTemperatureFacet.class, 0.5f);
        CoreBiome.MOUNTAINS.setLimits(SurfaceHumidityFacet.class, 0.2f, 0.6f);
        CoreBiome.MOUNTAINS.setUpperLimit(SurfaceTemperatureFacet.class, 0.5f);
        CoreBiome.FOREST.setLowerLimit(SurfaceHumidityFacet.class, 0.3f);
        CoreBiome.FOREST.setLimits(SurfaceTemperatureFacet.class, 0.4f, 0.7f);
        Stream.of(CoreBiome.values()).forEach(biomeRegistry::registerBiome);
    }
}
