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
import com.github.begla.blockmania.main.BlockmaniaConfiguration;
import com.github.begla.blockmania.world.chunk.Chunk;

/**
 * Generates some trees, flowers and high grass.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorFlora extends ChunkGeneratorTerrain {

    /**
     * Init. the generator with a given seed value.
     */
    public ChunkGeneratorFlora(GeneratorManager generatorManager) {
        super(generatorManager);
    }

    /**
     * Apply the generation process to the given chunk.
     *
     * @param c
     */
    @Override
    public void generate(Chunk c) {
        for (int y = 0; y < Chunk.getChunkDimensionY(); y++) {
            for (int x = 0; x < Chunk.getChunkDimensionX(); x++) {
                for (int z = 0; z < Chunk.getChunkDimensionZ(); z++) {
                    generateGrassAndFlowers(c, x, y, z);
                }
            }
        }

        generateTreesAndCacti(c);
    }

    private void generateTreesAndCacti(Chunk c) {
        for (int y = 32; y < Chunk.getChunkDimensionY(); y++) {
            for (int x = 0; x < Chunk.getChunkDimensionX(); x += 4) {
                for (int z = 0; z < Chunk.getChunkDimensionZ(); z += 4) {

                    double rand = (_parent.getParent().getRandom().randomDouble() + 1.0) / 2.0;
                    double prob = 1.0;

                    BIOME_TYPE biome = calcBiomeTypeForGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));
                    double humidity = calcHumidityAtGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));
                    double temperature = calcTemperatureAtGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));

                    switch (biome) {
                        case PLAINS:
                            prob = 1.0 - (Double) BlockmaniaConfiguration.getInstance().getConfig().get("World.Biomes.Plains.treeDensity");
                            break;
                        case MOUNTAINS:
                            prob = 1.0 - (Double) BlockmaniaConfiguration.getInstance().getConfig().get("World.Biomes.Mountains.treeDensity");
                            break;
                        case FOREST:
                            prob = 1.0 - (Double) BlockmaniaConfiguration.getInstance().getConfig().get("World.Biomes.Forest.treeDensity");
                            break;
                        case SNOW:
                            prob = 1.0 - (Double) BlockmaniaConfiguration.getInstance().getConfig().get("World.Biomes.Snow.treeDensity");
                            break;
                        case DESERT:
                            prob = 1.0 - (Double) BlockmaniaConfiguration.getInstance().getConfig().get("World.Biomes.Desert.treeDensity");
                            break;
                    }

                    if (rand > prob) {
                        int randX = x + _parent.getParent().getRandom().randomInt() % 12 + 6;
                        int randZ = z + _parent.getParent().getRandom().randomInt() % 12 + 6;

                        if (temperature > 0.55 && humidity < 0.33 && (c.getBlock(randX, y, randZ) == BlockManager.getInstance().getBlock("Grass").getId() || c.getBlock(randX, y, randZ) == BlockManager.getInstance().getBlock("Snow").getId() || c.getBlock(randX, y, randZ) == BlockManager.getInstance().getBlock("Sand").getId()))
                            _parent.getTreeGenerators().get(0).generate(c.getBlockWorldPosX(randX), y + 1, c.getBlockWorldPosZ(randZ), false);
                        else if (c.getBlock(randX, y, randZ) == BlockManager.getInstance().getBlock("Grass").getId() || c.getBlock(randX, y, randZ) == BlockManager.getInstance().getBlock("Snow").getId())
                            generateTree(c, randX, y, randZ);
                    }
                }
            }
        }
    }

    /**
     * @param c
     * @param x
     * @param y
     * @param z
     */

    void generateGrassAndFlowers(Chunk c, int x, int y, int z) {
        if (c.getBlock(x, y, z) == BlockManager.getInstance().getBlock("Grass").getId() && c.getBlock(x, y + 1, z) == 0x0) {

            double grassRand = (_parent.getParent().getRandom().randomDouble() + 1.0) / 2.0;
            double grassProb = 1.0;

            BIOME_TYPE biome = calcBiomeTypeForGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));

            switch (biome) {
                case PLAINS:
                    grassProb = 1.0 - (Double) BlockmaniaConfiguration.getInstance().getConfig().get("World.Biomes.Plains.grassDensity");
                    break;
                case MOUNTAINS:
                    grassProb = 1.0 - (Double) BlockmaniaConfiguration.getInstance().getConfig().get("World.Biomes.Mountains.grassDensity");
                    break;
                case FOREST:
                    grassProb = 1.0 - (Double) BlockmaniaConfiguration.getInstance().getConfig().get("World.Biomes.Forest.grassDensity");
                    break;
                case SNOW:
                    grassProb = 1.0 - (Double) BlockmaniaConfiguration.getInstance().getConfig().get("World.Biomes.Snow.grassDensity");
                    break;
                case DESERT:
                    grassProb = 1.0 - (Double) BlockmaniaConfiguration.getInstance().getConfig().get("World.Biomes.Desert.grassDensity");
                    break;
            }

            if (grassRand > grassProb) {
                /*
                 * Generate high grass.
                 */
                double rand = _parent.getParent().getRandom().standNormalDistrDouble();
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
                if (_parent.getParent().getRandom().standNormalDistrDouble() < -2) {
                    if (_parent.getParent().getRandom().randomBoolean()) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("Red flower").getId());
                    } else {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("Yellow flower").getId());
                    }

                }
            }
        }
    }

    /**
     * @param c
     * @param x
     * @param y
     * @param z
     */
    void generateTree(Chunk c, int x, int y, int z) {
        if (!c.canBlockSeeTheSky(x, y + 1, z))
            return;

        int randomGeneratorId = 1;

        if (_parent.getTreeGenerators().size() > 2) {
            randomGeneratorId = 1 + Math.abs(_parent.getParent().getRandom().randomInt()) % (_parent.getTreeGenerators().size() - 1);
        }

        double rand = Math.abs(_parent.getParent().getRandom().randomDouble());
        TreeGenerator treeGen = _parent.getTreeGenerators().get(randomGeneratorId);

        if (rand < treeGen.getGenProbability())
            treeGen.generate(c.getBlockWorldPosX(x), y + 1, c.getBlockWorldPosZ(z), false);
    }
}
