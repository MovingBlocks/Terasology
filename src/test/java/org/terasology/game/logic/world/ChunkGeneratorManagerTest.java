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

package org.terasology.game.logic.world;

import org.junit.Test;
import org.terasology.logic.world.*;
import org.terasology.logic.world.generator.ChunkGenerator;
import org.terasology.logic.world.generator.core.ChunkGeneratorManager;
import org.terasology.logic.world.generator.core.ChunkGeneratorManagerImpl;
import org.terasology.math.Vector3i;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Immortius
 */
public class ChunkGeneratorManagerTest {

    private final static String Seed = "Seed";

    @Test
    public void registeredGeneratorsReceivesSeed() {
        WorldBiomeProvider biomeProvider = mock(WorldBiomeProvider.class);
        ChunkGeneratorManager generatorManager = new ChunkGeneratorManagerImpl(Seed, biomeProvider);
        ChunkGenerator generator = mock(ChunkGenerator.class);
        generatorManager.registerChunkGenerator(generator);
        verify(generator).setWorldSeed(Seed);
    }

    @Test
    public void registeredGeneratorReceivesBiomeProvider() {
        WorldBiomeProvider biomeProvider = mock(WorldBiomeProvider.class);
        ChunkGeneratorManager generatorManager = new ChunkGeneratorManagerImpl(Seed, biomeProvider);
        ChunkGenerator generator = mock(ChunkGenerator.class);
        generatorManager.registerChunkGenerator(generator);
        verify(generator).setWorldBiomeProvider(biomeProvider);
    }

    @Test
    public void changeSeedPropagatedToChunkGenerator() {
        WorldBiomeProvider biomeProvider = mock(WorldBiomeProvider.class);
        ChunkGeneratorManager generatorManager = new ChunkGeneratorManagerImpl(Seed, biomeProvider);
        ChunkGenerator generator = mock(ChunkGenerator.class);
        generatorManager.registerChunkGenerator(generator);
        generatorManager.setWorldSeed("Seed2");
        verify(generator).setWorldSeed("Seed2");
    }

    @Test
    public void changeBiomeProviderToChunkGenerator() {
        WorldBiomeProvider biomeProvider = mock(WorldBiomeProvider.class);
        ChunkGeneratorManager generatorManager = new ChunkGeneratorManagerImpl(Seed, biomeProvider);
        ChunkGenerator generator = mock(ChunkGenerator.class);
        generatorManager.registerChunkGenerator(generator);
        WorldBiomeProvider newBiomeProvider = mock(WorldBiomeProvider.class);
        generatorManager.setWorldBiomeProvider(newBiomeProvider);
        verify(generator).setWorldBiomeProvider(newBiomeProvider);
    }

    @Test
    public void createChunkPassesThroughGenerator() {
        WorldBiomeProvider biomeProvider = mock(WorldBiomeProvider.class);
        ChunkGeneratorManager generatorManager = new ChunkGeneratorManagerImpl(Seed, biomeProvider);
        ChunkGenerator generator = mock(ChunkGenerator.class);
        generatorManager.registerChunkGenerator(generator);

        Vector3i pos = new Vector3i(3,4,6);
        Chunk chunk = generatorManager.generateChunk(pos);
        assertNotNull(chunk);
        assertEquals(pos, chunk.getPos());

        verify(generator).generateChunk(chunk);
    }


}
