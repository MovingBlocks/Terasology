/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.world.generation;

import org.terasology.engine.SimpleUri;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generator.ScalableWorldGenerator;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.zones.Zone;

import java.util.List;

/**
 * The most commonly used implementation of {@link WorldGenerator} based on the idea of Facets
 */
public abstract class BaseFacetedWorldGenerator implements ScalableWorldGenerator {

    protected WorldBuilder worldBuilder;

    private final SimpleUri uri;

    private String worldSeed;
    private World world;

    private FacetedWorldConfigurator configurator;

    public BaseFacetedWorldGenerator(SimpleUri uri) {
        this.uri = uri;
    }

    @Override
    public final SimpleUri getUri() {
        return uri;
    }

    @Override
    public String getWorldSeed() {
        return worldSeed;
    }

    @Override
    public void setWorldSeed(final String seed) {
        worldSeed = seed;
        getWorldBuilder().setSeed(seed.hashCode());

        // reset the world to lazy load it again later
        world = null;
    }

    /**
     * @return New {@link WorldBuilder} for building the current world
     */
    protected abstract WorldBuilder createWorld();

    @Override
    public void initialize() {
        getWorld().initialize();
    }

    @Override
    public void createChunk(CoreChunk chunk, EntityBuffer buffer) {
        world.rasterizeChunk(chunk, buffer);
    }

    @Override
    public void createChunk(CoreChunk chunk, EntityBuffer buffer, float scale) {
        world.rasterizeChunk(chunk, buffer, scale);
    }

    @Override
    public WorldConfigurator getConfigurator() {
        if (configurator == null) {
            configurator = getWorldBuilder().createConfigurator();
        }
        return configurator;
    }

    @Override
    public World getWorld() {
        // build the world as late as possible so that we can do configuration and 2d previews
        if (world == null) {
            world = getWorldBuilder().build();
        }
        return world;
    }

    /**
     * Returns current {@link WorldBuilder} or a new one if none has been created so far
     * @return WorldBuilder used by this WorldGenerator
     */
    private WorldBuilder getWorldBuilder() {
        if (worldBuilder == null) {
            worldBuilder = createWorld();
        }
        return worldBuilder;
    }

    @Override
    public List<Zone> getZones() {
        return getWorldBuilder().getChildZones();
    }

    @Override
    public Zone getNamedZone(String name) {
        return getWorldBuilder().getChildZone(name);
    }
}
