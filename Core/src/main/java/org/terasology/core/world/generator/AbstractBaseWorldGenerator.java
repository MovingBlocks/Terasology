/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.core.world.generator;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.EntityBuffer;
import org.terasology.world.generation.World;
import org.terasology.world.generator.ChunkGenerationPass;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldConfiguratorAdapter;
import org.terasology.world.generator.WorldGenerator;

import java.util.List;

/**
 */
public abstract class AbstractBaseWorldGenerator implements WorldGenerator {
    private static final Logger logger = LoggerFactory.getLogger(AbstractBaseWorldGenerator.class);

    private String worldSeed;
    private final List<ChunkGenerationPass> generationPasses = Lists.newArrayList();
    private final SimpleUri uri;

    public AbstractBaseWorldGenerator(SimpleUri uri) {
        this.uri = uri;
    }

    @Override
    public void initialize() {
        // do nothing
    }

    @Override
    public WorldConfigurator getConfigurator() {
        return new WorldConfiguratorAdapter();
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
        for (final ChunkGenerationPass generator : generationPasses) {
            generator.setWorldSeed(seed);
        }
    }

    protected final void register(final ChunkGenerationPass generator) {
        registerPass(generator);
        generationPasses.add(generator);
    }

    private void registerPass(final ChunkGenerationPass generator) {
        generator.setWorldSeed(worldSeed);
    }

    @Override
    public void createChunk(final CoreChunk chunk, EntityBuffer buffer) {
        for (final ChunkGenerationPass generator : generationPasses) {
            try {
                generator.generateChunk(chunk);
            } catch (RuntimeException e) {
                logger.error("Error during generation pass {}", generator, e);
            }
        }
    }

    @Override
    public World getWorld() {
        return null;
    }
}
