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
package org.terasology.logic.generators;

import org.terasology.logic.manager.Config;
import org.terasology.logic.newWorld.generator.TreeGenerator;
import org.terasology.logic.world.Chunk;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.utilities.FastRandom;

/**
 * Generates some trees, flowers and high grass.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorFlora extends ChunkGeneratorTerrain {

    private static final double DESERT_GRASS_DENSITY = Config.getInstance().getDesertGrassDensity();
    private static final double FOREST_GRASS_DENSITY = Config.getInstance().getForestGrassDensity();
    private static final double PLAINS_GRASS_DENSITY = Config.getInstance().getPlainsGrassDensity();
    private static final double SNOW_GRASS_DENSITY = Config.getInstance().getSnowGrassDensity();
    private static final double MOUNTAINS_GRASS_DENSITY = Config.getInstance().getMountainGrassDensity();

    private int worldSeed;

    public ChunkGeneratorFlora(GeneratorManager generatorManager, int worldSeed) {
        super(generatorManager);
        this.worldSeed = worldSeed;
    }

    @Override
    public void generate(Chunk c) {
        FastRandom random = new FastRandom(worldSeed + c.getPos().hashCode());
        for (int y = 0; y < Chunk.CHUNK_DIMENSION_Y; y++) {
            for (int x = 0; x < Chunk.CHUNK_DIMENSION_X; x++) {
                for (int z = 0; z < Chunk.CHUNK_DIMENSION_Z; z++) {
                    generateGrassAndFlowers(c, x, y, z, random);
                }
            }
        }

        generateTrees(c, random);
    }

    /**
     * Generates trees on the given chunk.
     *
     * @param c The chunk
     */
    private void generateTrees(Chunk c, FastRandom random) {

        for (int y = 32; y < Chunk.CHUNK_DIMENSION_Y; y++) {
            for (int x = 0; x < Chunk.CHUNK_DIMENSION_X; x += 4) {
                for (int z = 0; z < Chunk.CHUNK_DIMENSION_Z; z += 4) {
                    BIOME_TYPE biome = calcBiomeTypeForGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));

                    int randX = x + random.randomInt() % 12 + 6;
                    int randZ = z + random.randomInt() % 12 + 6;

                    if (c.getBlock(randX, y, randZ) == BlockManager.getInstance().getBlock("Grass").getId() || c.getBlock(randX, y, randZ) == BlockManager.getInstance().getBlock("Snow").getId() || c.getBlock(randX, y, randZ) == BlockManager.getInstance().getBlock("Sand").getId()) {
                        double rand = Math.abs(random.randomDouble());

                        int randomGeneratorId;
                        int size = _parent.getTreeGenerators(biome).size();

                        if (size > 0) {
                            randomGeneratorId = Math.abs(random.randomInt()) % size;

                            TreeGenerator treeGen = _parent.getTreeGenerator(biome, randomGeneratorId);

                            if (rand < treeGen.getGenerationProbability()) {
                                generateTree(c, treeGen, randX, y, randZ, random);
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
    private void generateGrassAndFlowers(Chunk c, int x, int y, int z, FastRandom random) {
        if ((c.getBlock(x, y, z) == BlockManager.getInstance().getBlock("Grass").getId() || c.getBlock(x, y, z) == BlockManager.getInstance().getBlock("Sand").getId() || c.getBlock(x, y, z) == BlockManager.getInstance().getBlock("Snow").getId()) && c.getBlock(x, y + 1, z) == 0x0) {

            double grassRand = (random.randomDouble() + 1.0) / 2.0;
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
                 * Generate tall grass.
                 */
                double rand = random.standNormalDistrDouble();

                if (rand > -0.4 && rand < 0.4) {
                    c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("TallGrass1").getId());
                } else if (rand > -0.6 && rand < 0.6) {
                    c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("TallGrass2").getId());
                } else {
                    c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("TallGrass3").getId());
                }

                double flowerRand = random.randomDouble();

                /*
                 * Generate flowers.
                 */
                if (random.standNormalDistrDouble() < -2) {
                    if (flowerRand >= -1.0 && flowerRand < 0.2) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("RedFlower").getId());
                    } else if (flowerRand >= 0.2 && flowerRand < 0.6) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("YellowFlower").getId());
                    } else if (flowerRand >= 0.6 && flowerRand < 0.7) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("BrownShroom").getId());
                    } else if (flowerRand >= 0.7 && flowerRand < 0.8) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("RedShroom").getId());
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
    private void generateTree(Chunk c, TreeGenerator treeGen, int x, int y, int z, FastRandom random) {
        if (!c.canBlockSeeTheSky(x, y + 1, z))
            return;

        //treeGen.generate(random, c.getBlockWorldPosX(x), y + 1, c.getBlockWorldPosZ(z), false);
    }
}
