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

package org.terasology.logic.world.generator.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.terasology.logic.generators.DefaultGenerators;
import org.terasology.logic.world.WorldBiomeProvider;
import org.terasology.logic.world.WorldView;
import org.terasology.logic.world.chunks.Chunk;
import org.terasology.logic.world.generator.BaseChunkGenerator;
import org.terasology.logic.world.generator.ChunkGenerator;
import org.terasology.logic.world.generator.SecondPassChunkGenerator;
import org.terasology.logic.world.liquid.LiquidsGenerator;
import org.terasology.math.Vector3i;

import com.google.common.collect.Lists;

/**
 * @author Immortius
 */
public class ChunkGeneratorManagerImpl implements ChunkGeneratorManager {

    private String worldSeed;
    private WorldBiomeProvider biomeProvider;
    private final List<ChunkGenerator> chunkGenerators = Lists.newArrayList();
    private final List<SecondPassChunkGenerator> secondPassChunkGenerators = Lists.newArrayList();

    public ChunkGeneratorManagerImpl() {
    }

    public static ChunkGeneratorManagerImpl getDefaultInstance() {
        final ChunkGeneratorManagerImpl chunkGeneratorManager = new ChunkGeneratorManagerImpl();
        chunkGeneratorManager.registerChunkGenerator(new PerlinTerrainGenerator());
        chunkGeneratorManager.registerChunkGenerator(new FloraGenerator());
        chunkGeneratorManager.registerChunkGenerator(new LiquidsGenerator());
        final ForestGenerator forestGen = new ForestGenerator();
        new DefaultGenerators(forestGen);
        chunkGeneratorManager.registerChunkGenerator(forestGen);

        return chunkGeneratorManager;
    }
    
    public static ChunkGeneratorManagerImpl buildChunkGenerator(List<String> list) {
    	final ChunkGeneratorManagerImpl chunkGeneratorManager = new ChunkGeneratorManagerImpl();
    	
    	for (String generator : list) {
			try {
				BaseChunkGenerator chunkGenerator = null;
				Class<?> [] classParm = null;
				Object [] objectParm = null;
				
				Constructor<?> c = Class.forName(generator).getConstructor(classParm);
				chunkGenerator = (BaseChunkGenerator) c.newInstance(objectParm);
				
				if (chunkGenerator instanceof ForestGenerator) {
					new DefaultGenerators((ForestGenerator) chunkGenerator);
				}
				
				chunkGeneratorManager.registerChunkGenerator(chunkGenerator);
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

    	return chunkGeneratorManager;
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

    @Override
    public void registerChunkGenerator(final BaseChunkGenerator generator) {
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
