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

import com.github.begla.blockmania.main.Configuration;
import com.github.begla.blockmania.world.chunk.Chunk;

/**
 * Generates some trees, flowers and high grass.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorFlora extends ChunkGeneratorTerrain {

    /**
     * Init. the forest generator.
     *
     * @param seed
     */
    public ChunkGeneratorFlora(String seed) {
        super(seed);
    }

    /**
     * Apply the generation process to the given chunk.
     *
     * @param c
     */
    @Override
    public void generate(Chunk c) {
        for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
            for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                    generateGrassAndFlowers(c, x, y, z);
                }
            }
        }

        generateTreesAndCacti(c);
    }

    private void generateTreesAndCacti(Chunk c) {
        for (int y = 32; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
            for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x += 4) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z += 4) {

                    double rand = (_rand.randomDouble() + 1.0) / 2.0;
                    double prob = 1.0;

                    BIOME_TYPE biome = calcBiomeTypeForGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));
                    double humidity = calcHumidityAtGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));
                    double temperature = calcTemperatureAtGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));

                    switch (biome) {
                        case PLAINS:
                            prob = 0.9;
                            break;
                        case MOUNTAINS:
                            prob = 0.8;
                            break;
                        case SNOW:
                            prob = 0.8;
                            break;
                        case FOREST:
                            prob = 0.1;
                            break;
                        case DESERT:
                            prob = 0.9;
                            break;
                    }

                    if (rand > prob) {
                        int randX = x + _rand.randomInt() % 12 + 6;
                        int randZ = z + _rand.randomInt() % 12 + 6;

                        if (temperature > 0.55 && humidity < 0.33 && (c.getBlock(randX, y, randZ) == 0x1 || c.getBlock(randX, y, randZ) == 0x17 || c.getBlock(randX, y, randZ) == 0x7))
                            c.getParent().getObjectGenerator("cactus").generate(c.getBlockWorldPosX(randX), y + 1, c.getBlockWorldPosZ(randZ), false);
                        else if (c.getBlock(randX, y, randZ) == 0x1 || c.getBlock(randX, y, randZ) == 0x17)
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

        if (c.getBlock(x, y, z) == 0x1) {

            double grassRand = (_rand.randomDouble() + 1.0) / 2.0;
            double grassProb = 1.0;

            BIOME_TYPE biome = calcBiomeTypeForGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));

            switch (biome) {
                case PLAINS:
                    grassProb = 0.7;
                    break;
                case MOUNTAINS:
                    grassProb = 0.8;
                    break;
                case FOREST:
                    grassProb = 0.8;
                    break;
            }

            if (grassRand > grassProb) {
                /*
                 * Generate high grass.
                 */
                double rand = _rand.standNormalDistrDouble();
                if (rand > -0.4 && rand < 0.4) {
                    c.setBlock(x, y + 1, z, (byte) 0xB);
                } else if (rand > -0.6 && rand < 0.6) {
                    c.setBlock(x, y + 1, z, (byte) 0x24);
                } else {
                    c.setBlock(x, y + 1, z, (byte) 0xC);
                }

                /*
                 * Generate flowers.
                 */
                if (_rand.standNormalDistrDouble() < -2) {
                    if (_rand.randomBoolean()) {
                        c.setBlock(x, y + 1, z, (byte) 0x9);
                    } else {
                        c.setBlock(x, y + 1, z, (byte) 0xA);
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
        // Trees should only be placed in direct sunlight
        if (!c.canBlockSeeTheSky(x, y + 1, z))
            return;

        double r2 = _rand.standNormalDistrDouble();
        if (r2 > -2 && r2 < -1) {
            c.setBlock(x, y + 1, z, (byte) 0x0);
            c.getParent().getObjectGenerator("pineTree").generate(c.getBlockWorldPosX(x), y + 1, c.getBlockWorldPosZ(z), false);
        } else if (r2 > 1 && r2 < 2) {
            c.setBlock(x, y + 1, z, (byte) 0x0);
            c.getParent().getObjectGenerator("firTree").generate(c.getBlockWorldPosX(x), y + 1, c.getBlockWorldPosZ(z), false);
        } else {
            c.setBlock(x, y + 1, z, (byte) 0x0);
            c.getParent().getObjectGenerator("tree").generate(c.getBlockWorldPosX(x), y + 1, c.getBlockWorldPosZ(z), false);
        }
    }
}
