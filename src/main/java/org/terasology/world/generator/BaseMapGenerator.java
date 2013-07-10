/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.world.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.generators.DefaultGenerators;
import org.terasology.math.Vector3i;
import org.terasology.rendering.gui.widgets.UIDialog;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.WorldView;
import org.terasology.world.chunks.Chunk;

import com.google.common.collect.Lists;
import org.terasology.world.generator.core.ForestGenerator;

/**
 * @author Immortius
 */
public abstract class BaseMapGenerator implements MapGenerator {
    private static final Logger logger = LoggerFactory.getLogger(BaseMapGenerator.class);
    private String worldSeed;
    private WorldBiomeProvider biomeProvider;
    private final List<ChunkGenerator> chunkGenerators = Lists.newArrayList();
    private final List<SecondPassChunkGenerator> secondPassChunkGenerators = Lists.newArrayList();
    private final MapGeneratorUri uri;

    protected BaseMapGenerator(MapGeneratorUri uri) {
        this.uri = uri;
    }

    @Override
    public MapGeneratorUri uri() {
        return uri;
    }

    @Override
    public boolean hasSetup() {
        return false;
    }

    @Override
    public UIDialog createSetupDialog() {
        return null;
    }

    @Override
    public void setWorldSeed(final String seed) {
        worldSeed = seed;
        for (final BaseChunkGenerator generator : chunkGenerators) {
            generator.setWorldSeed(seed);
        }
        for (final BaseChunkGenerator generator : secondPassChunkGenerators) {
            generator.setWorldSeed(seed);
        }
    }

    @Override
    public void setWorldBiomeProvider(final WorldBiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
        for (final BaseChunkGenerator generator : chunkGenerators) {
            generator.setWorldBiomeProvider(biomeProvider);
        }
        for (final BaseChunkGenerator generator : secondPassChunkGenerators) {
            generator.setWorldBiomeProvider(biomeProvider);
        }
    }

    public void registerChunkGenerator(final BaseChunkGenerator generator) {
        generator.setWorldBiomeProvider(biomeProvider);
        generator.setWorldSeed(worldSeed);
        if (generator instanceof ChunkGenerator) {
            chunkGenerators.add((ChunkGenerator) generator);
        }
        if (generator instanceof SecondPassChunkGenerator) {
            secondPassChunkGenerators.add((SecondPassChunkGenerator) generator);
        }
        // TODO move this code somewhere else
        if (generator instanceof ForestGenerator) {
            ForestGenerator forestGenerator = (ForestGenerator) generator;
            new DefaultGenerators(forestGenerator);
        }
    }

    public List<BaseChunkGenerator> getBaseChunkGenerators() {
        final List<BaseChunkGenerator> baseChunkGenerators = new ArrayList<BaseChunkGenerator>();
        baseChunkGenerators.addAll(chunkGenerators);
        baseChunkGenerators.addAll(secondPassChunkGenerators);
        return baseChunkGenerators;
    }

    @Override
    public Chunk generateChunk(final Vector3i pos) {
        final Chunk chunk = new Chunk(pos);
        for (final ChunkGenerator generator : chunkGenerators) {
            generator.generateChunk(chunk);
        }
        return chunk;
    }

    @Override
    public void secondPassChunk(final Vector3i chunkPos, final WorldView view) {
        for (final SecondPassChunkGenerator generator : secondPassChunkGenerators) {
            generator.postProcessChunk(chunkPos, view);
        }
    }
}
