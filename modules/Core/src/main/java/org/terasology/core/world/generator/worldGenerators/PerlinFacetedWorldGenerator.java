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
package org.terasology.core.world.generator.worldGenerators;

import com.google.common.base.Optional;
import org.terasology.core.world.generator.perlinFacetProviders.PerlinBaseSurfaceProvider;
import org.terasology.core.world.generator.perlinFacetProviders.PerlinBiomeProvider;
import org.terasology.core.world.generator.perlinFacetProviders.PerlinFloraProvider;
import org.terasology.core.world.generator.perlinFacetProviders.PerlinHillsAndMountainsProvider;
import org.terasology.core.world.generator.perlinFacetProviders.PerlinHumidityProvider;
import org.terasology.core.world.generator.perlinFacetProviders.PerlinOceanProvider;
import org.terasology.core.world.generator.perlinFacetProviders.PerlinRiverProvider;
import org.terasology.core.world.generator.perlinFacetProviders.PerlinSeaLevelProvider;
import org.terasology.core.world.generator.perlinFacetProviders.PerlinSurfaceToDensityProvider;
import org.terasology.core.world.generator.perlinFacetProviders.PerlinTemperatureProvider;
import org.terasology.core.world.generator.perlinFacetProviders.PerlinTreeProvider;
import org.terasology.core.world.generator.rasterizers.FloraRasterizer;
import org.terasology.core.world.generator.rasterizers.SolidRasterizer;
import org.terasology.core.world.generator.rasterizers.TreeRasterizer;
import org.terasology.engine.SimpleUri;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.World;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;

/**
 * @author Immortius
 */
@RegisterWorldGenerator(id = "facetedperlin", displayName = "Perlin Faceted", description = "Faceted world generator")
public class PerlinFacetedWorldGenerator implements WorldGenerator {

    private final SimpleUri uri;
    private String worldSeed;
    private World world;

    public PerlinFacetedWorldGenerator(SimpleUri uri) {
        this.uri = uri;
    }

    @Override
    public final SimpleUri getUri() {
        return uri;
    }

    @Override
    public void setWorldSeed(final String seed) {
        worldSeed = seed;
    }


    @Override
    public float getFog(float x, float y, float z) {
        return 0;
    }

    @Override
    public float getTemperature(float x, float y, float z) {
        return 0.5f;
    }

    @Override
    public float getHumidity(float x, float y, float z) {
        return 0.5f;
    }

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        world = new WorldBuilder(worldSeed.hashCode())
                .addProvider(new PerlinSeaLevelProvider())
                .addProvider(new PerlinHumidityProvider())
                .addProvider(new PerlinTemperatureProvider())
                .addProvider(new PerlinBaseSurfaceProvider())
                .addProvider(new PerlinRiverProvider())
                .addProvider(new PerlinOceanProvider())
                .addProvider(new PerlinHillsAndMountainsProvider())
                .addProvider(new PerlinBiomeProvider())
                .addProvider(new PerlinSurfaceToDensityProvider())
                .addProvider(new PerlinFloraProvider())
                .addProvider(new PerlinTreeProvider())
                        //.addRasterizer(new GroundRasterizer(blockManager))
                .addRasterizer(new TreeRasterizer(blockManager))
                .addRasterizer(new SolidRasterizer(blockManager))
                .addRasterizer(new FloraRasterizer(blockManager))
                .build();
    }

    @Override
    public void createChunk(CoreChunk chunk) {
        world.rasterizeChunk(chunk);
    }

    @Override
    public Optional<WorldConfigurator> getConfigurator() {
        return Optional.absent();
    }
}
