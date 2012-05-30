/*
 * Copyright 2012
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

package org.terasology.logic.world;

import com.google.common.collect.Lists;
import org.terasology.math.Vector3i;

import java.util.List;

/**
 * @author Immortius
 */
public class ChunkGeneratorManagerImpl implements ChunkGeneratorManager {

    private String worldSeed;
    private WorldBiomeProvider biomeProvider;
    private List<ChunkGenerator> chunkGenerators = Lists.newArrayList();
    private List<SecondPassChunkGenerator> secondPassChunkGenerators = Lists.newArrayList();

    public ChunkGeneratorManagerImpl(String seed, WorldBiomeProvider biomeProvider) {
        this.worldSeed = seed;
        this.biomeProvider = biomeProvider;
    }

    @Override
    public void setWorldSeed(String seed) {
        this.worldSeed = seed;
        for (BaseChunkGenerator generator : chunkGenerators) {
            generator.setWorldSeed(seed);
        }
        for (BaseChunkGenerator generator : secondPassChunkGenerators) {
            generator.setWorldSeed(seed);
        }
    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
        for (BaseChunkGenerator generator : chunkGenerators) {
            generator.setWorldBiomeProvider(biomeProvider);
        }
        for (BaseChunkGenerator generator : secondPassChunkGenerators) {
            generator.setWorldBiomeProvider(biomeProvider);
        }
    }

    @Override
    public void registerChunkGenerator(BaseChunkGenerator generator) {
        generator.setWorldBiomeProvider(biomeProvider);
        generator.setWorldSeed(worldSeed);
        if (generator instanceof ChunkGenerator) {
            chunkGenerators.add((ChunkGenerator) generator);
        }
        if (generator instanceof SecondPassChunkGenerator) {
            secondPassChunkGenerators.add((SecondPassChunkGenerator) generator);
        }
    }

    @Override
    public Chunk generateChunk(Vector3i pos) {
        Chunk chunk = new Chunk(pos);
        for (ChunkGenerator generator : chunkGenerators) {
            generator.generateChunk(chunk);
        }
        return chunk;
    }

    @Override
    public void secondPassChunk(Vector3i chunkPos, WorldView view) {
        for (SecondPassChunkGenerator generator : secondPassChunkGenerators) {
            generator.postProcessChunk(chunkPos, view);
        }
    }
}
