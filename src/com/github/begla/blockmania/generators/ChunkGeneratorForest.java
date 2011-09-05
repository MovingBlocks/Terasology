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

import com.github.begla.blockmania.Configuration;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.world.Chunk;

/**
 * Generates some trees, flowers and high grass.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorForest extends ChunkGeneratorTerrain {

    /**
     * Init. the forest generator.
     *
     * @param seed
     */
    public ChunkGeneratorForest(String seed) {
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

        FastRandom rand = new FastRandom(c.getChunkId());

        for (int y = 32; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
            for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x += 4) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z += 4) {
                    float forestDens = calcForestDensity(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));

                    if (forestDens > 0.01) {

                        int randX = x + rand.randomInt() % 12;
                        int randZ = z + rand.randomInt() % 12;

                        if (c.getBlock(randX, y, randZ) == 0x1 || c.getBlock(randX, y, randZ) == 0x17) {
                            generateTree(c, randX, y, randZ);
                        } else if (c.getBlock(randX, y, randZ) == 0x7) {
                            c.getParent().getObjectGenerator("cactus").generate(c.getBlockWorldPosX(randX), c.getBlockWorldPosY(y) + 1, c.getBlockWorldPosZ(randZ), false);
                        }
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
            float grassDens = calcGrassDensity(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));

            if (grassDens > 0.0) {
                /*
                 * Generate high grass.
                 */
                double rand = _rand.standNormalDistrDouble();
                if (rand > -0.4 && rand < 0.4) {
                    c.setBlock(x, y + 1, z, (byte) 0xB);
                } else if (rand > -0.8 && rand < -0.8) {
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
        double r2 = _rand.standNormalDistrDouble();
        if (r2 > -2 && r2 < -1) {
            c.setBlock(x, y + 1, z, (byte) 0x0);
            c.getParent().getObjectGenerator("pineTree").generate(c.getBlockWorldPosX(x), c.getBlockWorldPosY(y) + 1, c.getBlockWorldPosZ(z), false);
        } else if (r2 > 1 && r2 < 2) {
            c.setBlock(x, y + 1, z, (byte) 0x0);
            c.getParent().getObjectGenerator("firTree").generate(c.getBlockWorldPosX(x), c.getBlockWorldPosY(y) + 1, c.getBlockWorldPosZ(z), false);
        } else {
            c.setBlock(x, y + 1, z, (byte) 0x0);
            c.getParent().getObjectGenerator("tree").generate(c.getBlockWorldPosX(x), c.getBlockWorldPosY(y) + 1, c.getBlockWorldPosZ(z), false);
        }
    }

    /**
     * Returns the cave density for the base terrain.
     *
     * @param x
     * @param z
     * @return
     */
    float calcForestDensity(float x, float z) {
        float result = 0.0f;
        result += _pGen3.multiFractalNoise(0.2f * x, 0f, 0.2f * z, 7, 2.3614521f);
        return result;
    }

    /**
     * @param x
     * @param z
     * @return
     */
    float calcGrassDensity(float x, float z) {
        float result = 0.0f;
        result += _pGen3.multiFractalNoise(0.01f * x, 0f, 0.01f * z, 7, 2.37152f);
        return result;
    }
}
