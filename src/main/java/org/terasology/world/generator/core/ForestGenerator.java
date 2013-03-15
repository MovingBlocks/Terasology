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

package org.terasology.world.generator.core;

import java.util.Map;

import org.terasology.game.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.WorldView;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.SecondPassChunkGenerator;
import org.terasology.world.generator.tree.TreeGenerator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author Immortius
 */
public class ForestGenerator implements SecondPassChunkGenerator {
    private String seed;
    private WorldBiomeProvider biomeProvider;

    private ListMultimap<WorldBiomeProvider.Biome, TreeGenerator> treeGenerators = ArrayListMultimap.create();

    private Block grassBlock;
    private Block snowBlock;
    private Block sandBlock;

    public ForestGenerator() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        grassBlock = blockManager.getBlock("engine:Grass");
        snowBlock = blockManager.getBlock("engine:Snow");
        sandBlock = blockManager.getBlock("engine:Sand");
    }

    public void addTreeGenerator(WorldBiomeProvider.Biome type, TreeGenerator gen) {
        treeGenerators.put(type, gen);
    }

    @Override
    public void postProcessChunk(Vector3i pos, WorldView view) {
        FastRandom random = new FastRandom(seed.hashCode() ^ (pos.x + 39L * (pos.y + 39L * pos.z)));
        for (int y = 32; y < Chunk.SIZE_Y; y++) {
            for (int x = 4; x < Chunk.SIZE_X; x += 4) {
                for (int z = 4; z < Chunk.SIZE_Z; z += 4) {
                    Vector3i worldPos = new Vector3i(pos);
                    worldPos.mult(new Vector3i(Chunk.SIZE_X, Chunk.SIZE_Y, Chunk.SIZE_Z));
                    worldPos.add(x, y, z);
                    WorldBiomeProvider.Biome biome = biomeProvider.getBiomeAt(worldPos.x, worldPos.z);

                    int randX = x + random.randomInt(3);
                    int randZ = z + random.randomInt(3);

                    Block posBlock = view.getBlock(randX, y, randZ);

                    if (posBlock.equals(sandBlock) || posBlock.equals(grassBlock) || posBlock.equals(snowBlock)) {
                        double rand = Math.abs(random.randomDouble());

                        int randomGeneratorId;
                        int size = treeGenerators.get(biome).size();

                        if (size > 0) {
                            randomGeneratorId = Math.abs(random.randomInt()) % size;

                            TreeGenerator treeGen = treeGenerators.get(biome).get(randomGeneratorId);

                            if (rand < treeGen.getGenerationProbability()) {
                                generateTree(view, treeGen, randX, y, randZ, random);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates a tree on the given chunk.
     *
     * @param treeGen The tree generator
     * @param x       Position on the x-axis
     * @param y       Position on the y-axis
     * @param z       Position on the z-axis
     */
    private void generateTree(WorldView view, TreeGenerator treeGen, int x, int y, int z, FastRandom random) {
        for (int checkY = y + 1; checkY < Chunk.SIZE_Y; ++checkY) {
            if (!view.getBlock(x, checkY, z).isTranslucent()) {
                return;
            }
        }
        treeGen.generate(view, random, x, y + 1, z);
    }

    @Override
    public void setWorldSeed(String seed) {
        this.seed = seed;
    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
    }

    @Override
    public Map<String, String> getInitParameters() {
        return null;
    }

    @Override
    public void setInitParameters(final Map<String, String> initParameters) {
    }

}
