/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.generators;

import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.configuration.ConfigurationManager;
import com.github.begla.blockmania.world.chunk.Chunk;

/**
 * Generates some trees, flowers and high grass.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorFlora extends ChunkGeneratorTerrain {

    private static final double DESERT_GRASS_DENSITY = (Double) ConfigurationManager.getInstance().getConfig().get("World.Biomes.Desert.grassDensity");
    private static final double FOREST_GRASS_DENSITY = (Double) ConfigurationManager.getInstance().getConfig().get("World.Biomes.Forest.grassDensity");
    private static final double PLAINS_GRASS_DENSITY = (Double) ConfigurationManager.getInstance().getConfig().get("World.Biomes.Plains.grassDensity");
    private static final double SNOW_GRASS_DENSITY = (Double) ConfigurationManager.getInstance().getConfig().get("World.Biomes.Snow.grassDensity");
    private static final double MOUNTAINS_GRASS_DENSITY = (Double) ConfigurationManager.getInstance().getConfig().get("World.Biomes.Mountains.grassDensity");

    public ChunkGeneratorFlora(GeneratorManager generatorManager) {
        super(generatorManager);
    }

    @Override
    public void generate(Chunk c) {
        for (int y = 0; y < Chunk.CHUNK_DIMENSION_Y; y++) {
            for (int x = 0; x < Chunk.CHUNK_DIMENSION_X; x++) {
                for (int z = 0; z < Chunk.CHUNK_DIMENSION_Z; z++) {
                    generateGrassAndFlowers(c, x, y, z);
                }
            }
        }

        generateTrees(c);
    }

    /**
     * Generates trees on the given chunk.
     *
     * @param c The chunk
     */
    private void generateTrees(Chunk c) {
        for (int y = 32; y < Chunk.CHUNK_DIMENSION_Y; y++) {
            for (int x = 0; x < Chunk.CHUNK_DIMENSION_X; x += 4) {
                for (int z = 0; z < Chunk.CHUNK_DIMENSION_Z; z += 4) {
                    BIOME_TYPE biome = calcBiomeTypeForGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));

                    int randX = x + c.getRandom().randomInt() % 12 + 6;
                    int randZ = z + c.getRandom().randomInt() % 12 + 6;

                    if (c.getBlock(randX, y, randZ) == BlockManager.getInstance().getBlock("Grass").getId() || c.getBlock(randX, y, randZ) == BlockManager.getInstance().getBlock("Snow").getId() || c.getBlock(randX, y, randZ) == BlockManager.getInstance().getBlock("Sand").getId()) {
                        double rand = Math.abs(c.getRandom().randomDouble());

                        int randomGeneratorId = 0;
                        int size = _parent.getTreeGenerators(biome).size();

                        if (size > 1) {
                            randomGeneratorId = Math.abs(c.getRandom().randomInt()) % size;

                            TreeGenerator treeGen = _parent.getTreeGenerator(biome, randomGeneratorId);

                            if (rand < treeGen.getGenProbability()) {
                                generateTree(c, treeGen, randX, y, randZ);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates grass or a flower on the given chunk.
     *
     * @param c The chunk
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @param z Position on the z-axis
     */
    private void generateGrassAndFlowers(Chunk c, int x, int y, int z) {
        if (c.getBlock(x, y, z) == BlockManager.getInstance().getBlock("Grass").getId() && c.getBlock(x, y + 1, z) == 0x0) {

            double grassRand = (c.getRandom().randomDouble() + 1.0) / 2.0;
            double grassProb = 1.0;

            BIOME_TYPE biome = calcBiomeTypeForGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));

            switch (biome) {
                case PLAINS:
                    grassProb = 1.0 - PLAINS_GRASS_DENSITY;
                    break;
                case MOUNTAINS:
                    grassProb = 1.0 - MOUNTAINS_GRASS_DENSITY;
                    break;
                case FOREST:
                    grassProb = 1.0 - FOREST_GRASS_DENSITY;
                    break;
                case SNOW:
                    grassProb = 1.0 - SNOW_GRASS_DENSITY;
                    break;
                case DESERT:
                    grassProb = 1.0 - DESERT_GRASS_DENSITY;
                    break;
            }

            if (grassRand > grassProb) {
                /*
                 * Generate high grass.
                 */
                double rand = c.getRandom().standNormalDistrDouble();
                if (rand > -0.4 && rand < 0.4) {
                    c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("High grass").getId());
                } else if (rand > -0.6 && rand < 0.6) {
                    c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("Medium high grass").getId());
                } else {
                    c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("Large high grass").getId());
                }

                /*
                 * Generate flowers.
                 */
                if (c.getRandom().standNormalDistrDouble() < -2) {
                    if (c.getRandom().randomBoolean()) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("Red flower").getId());
                    } else {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("Yellow flower").getId());
                    }
                }
            }
        }
    }

    /**
     * Generates a tree on the given chunk.
     *
     * @param c       The chunk
     * @param treeGen The tree generator
     * @param x       Position on the x-axis
     * @param y       Position on the y-axis
     * @param z       Position on the z-axis
     */
    private void generateTree(Chunk c, TreeGenerator treeGen, int x, int y, int z) {
        if (!c.canBlockSeeTheSky(x, y + 1, z))
            return;

        treeGen.generate(c.getRandom(), c.getBlockWorldPosX(x), y + 1, c.getBlockWorldPosZ(z), false);
    }
}
