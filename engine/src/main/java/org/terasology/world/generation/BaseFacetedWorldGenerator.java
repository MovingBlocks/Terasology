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
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;

public abstract class BaseFacetedWorldGenerator implements WorldGenerator {

    private final SimpleUri uri;

    private String worldSeed;
    private WorldBuilder worldBuilder;
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

    private WorldBuilder getWorldBuilder() {
        if (worldBuilder == null) {
            worldBuilder = createWorld();
        }
        return worldBuilder;
    }
}
