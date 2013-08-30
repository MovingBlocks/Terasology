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
package org.terasology.world.generator;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.math.Vector3i;
import org.terasology.world.ChunkView;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.chunks.Chunk;

import java.util.List;

/**
 * @author Immortius
 */
public class AbstractBaseWorldGenerator implements WorldGenerator {
    private static final Logger logger = LoggerFactory.getLogger(AbstractBaseWorldGenerator.class);

    private String worldSeed;
    private WorldBiomeProvider biomeProvider;
    private final List<FirstPassGenerator> firstPassGenerators = Lists.newArrayList();
    private final List<SecondPassGenerator> secondPassGenerators = Lists.newArrayList();
    private final SimpleUri uri;

    public AbstractBaseWorldGenerator(SimpleUri uri) {
        this.uri = uri;
    }

    @Override
    public final SimpleUri getUri() {
        return uri;
    }

    @Override
    public void setWorldSeed(final String seed) {
        worldSeed = seed;
        for (final BaseChunkGenerator generator : firstPassGenerators) {
            generator.setWorldSeed(seed);
        }
        for (final BaseChunkGenerator generator : secondPassGenerators) {
            generator.setWorldSeed(seed);
        }
    }

    @Override
    public void setWorldBiomeProvider(final WorldBiomeProvider value) {
        this.biomeProvider = value;
        for (final BaseChunkGenerator generator : firstPassGenerators) {
            generator.setWorldBiomeProvider(value);
        }
        for (final BaseChunkGenerator generator : secondPassGenerators) {
            generator.setWorldBiomeProvider(value);
        }
    }

    protected final void register(final FirstPassGenerator generator) {
        registerBaseChunkGenerator(generator);
        firstPassGenerators.add(generator);
    }

    protected final void register(final SecondPassGenerator generator) {
        registerBaseChunkGenerator(generator);
        secondPassGenerators.add(generator);
    }

    private void registerBaseChunkGenerator(final BaseChunkGenerator generator) {
        generator.setWorldBiomeProvider(biomeProvider);
        generator.setWorldSeed(worldSeed);
    }

    @Override
    public Chunk createChunk(final Vector3i pos) {
        final Chunk chunk = new Chunk(pos);
        for (final FirstPassGenerator generator : firstPassGenerators) {
            generator.generateChunk(chunk);
        }
        return chunk;
    }

    @Override
    public void applySecondPass(final Vector3i chunkPos, final ChunkView view) {
        for (final SecondPassGenerator generator : secondPassGenerators) {
            generator.postProcessChunk(chunkPos, view);
        }
    }
}
