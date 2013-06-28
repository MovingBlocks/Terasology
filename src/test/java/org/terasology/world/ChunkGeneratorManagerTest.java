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

package org.terasology.world;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.BaseMapGenerator;
import org.terasology.world.generator.ChunkGenerator;
import org.terasology.world.generator.MapGenerator;
import org.terasology.world.generator.MapGeneratorUri;

/**
 * @author Immortius
 */
public class ChunkGeneratorManagerTest {

    private final static String SEED = "Seed";
    private static class TestMapGenerator extends BaseMapGenerator {
        private TestMapGenerator() {
            super(new MapGeneratorUri("test:test"));
        }

        @Override
        public void setup() {
        }

        @Override
        public String name() {
            return "test";
        }

        @Override
        public boolean hasSetup() {
            return false;
        }
    }

    @Before
    public void setup() {
        CoreRegistry.put(Config.class, new Config());
    }

    @Test
    public void registeredGeneratorsReceivesSeed() {
        final WorldBiomeProvider biomeProvider = Mockito.mock(WorldBiomeProvider.class);
        final TestMapGenerator generatorManager = new TestMapGenerator();
        generatorManager.setWorldSeed(ChunkGeneratorManagerTest.SEED);
        generatorManager.setWorldBiomeProvider(biomeProvider);
        final ChunkGenerator generator = Mockito.mock(ChunkGenerator.class);
        generatorManager.registerChunkGenerator(generator);
        Mockito.verify(generator).setWorldSeed(ChunkGeneratorManagerTest.SEED);
    }

    @Test
    public void registeredGeneratorReceivesBiomeProvider() {
        final WorldBiomeProvider biomeProvider = Mockito.mock(WorldBiomeProvider.class);
        final TestMapGenerator generatorManager = new TestMapGenerator();
        generatorManager.setWorldSeed(ChunkGeneratorManagerTest.SEED);
        generatorManager.setWorldBiomeProvider(biomeProvider);
        final ChunkGenerator generator = Mockito.mock(ChunkGenerator.class);
        generatorManager.registerChunkGenerator(generator);
        Mockito.verify(generator).setWorldBiomeProvider(biomeProvider);
    }

    @Test
    public void changeSeedPropagatedToChunkGenerator() {
        final WorldBiomeProvider biomeProvider = Mockito.mock(WorldBiomeProvider.class);
        final TestMapGenerator generatorManager = new TestMapGenerator();
        generatorManager.setWorldSeed(ChunkGeneratorManagerTest.SEED);
        generatorManager.setWorldBiomeProvider(biomeProvider);
        final ChunkGenerator generator = Mockito.mock(ChunkGenerator.class);
        generatorManager.registerChunkGenerator(generator);
        generatorManager.setWorldSeed("Seed2");
        Mockito.verify(generator).setWorldSeed("Seed2");
    }

    @Test
    public void changeBiomeProviderToChunkGenerator() {
        final WorldBiomeProvider biomeProvider = Mockito.mock(WorldBiomeProvider.class);
        final TestMapGenerator generatorManager = new TestMapGenerator();
        generatorManager.setWorldSeed(ChunkGeneratorManagerTest.SEED);
        generatorManager.setWorldBiomeProvider(biomeProvider);
        final ChunkGenerator generator = Mockito.mock(ChunkGenerator.class);
        generatorManager.registerChunkGenerator(generator);
        final WorldBiomeProvider newBiomeProvider = Mockito.mock(WorldBiomeProvider.class);
        generatorManager.setWorldBiomeProvider(newBiomeProvider);
        Mockito.verify(generator).setWorldBiomeProvider(newBiomeProvider);
    }

    @Test
    public void createChunkPassesThroughGenerator() {
        final WorldBiomeProvider biomeProvider = Mockito.mock(WorldBiomeProvider.class);
        final TestMapGenerator generatorManager = new TestMapGenerator();
        generatorManager.setWorldSeed(ChunkGeneratorManagerTest.SEED);
        generatorManager.setWorldBiomeProvider(biomeProvider);
        final ChunkGenerator generator = Mockito.mock(ChunkGenerator.class);
        generatorManager.registerChunkGenerator(generator);

        final Vector3i pos = new Vector3i(3, 4, 6);
        final Chunk chunk = generatorManager.generateChunk(pos);
        Assert.assertNotNull(chunk);
        Assert.assertEquals(pos, chunk.getPos());

        Mockito.verify(generator).generateChunk(chunk);
    }

}
