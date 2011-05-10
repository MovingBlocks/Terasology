/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.github.begla.blockmania.generators;

import com.github.begla.blockmania.Chunk;
import com.github.begla.blockmania.Configuration;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.utilities.PerlinNoise;

/**
 * Generates the base terrain of the Blockmania world.
 * 
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorTerrain extends ChunkGenerator {

    public ChunkGeneratorTerrain(String seed) {
        super(seed);
    }

    /**
     *
     * @param c
     * @param parent
     */
    @Override
    public void generate(Chunk c) {
        int xOffset = (int) c.getPosition().x * (int) Configuration.CHUNK_DIMENSIONS.x;
        int yOffset = (int) c.getPosition().y * (int) Configuration.CHUNK_DIMENSIONS.y;
        int zOffset = (int) c.getPosition().z * (int) Configuration.CHUNK_DIMENSIONS.z;

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                int height = (int) (calcTerrainElevation(x + xOffset, z + zOffset) + (calcTerrainRoughness(x + xOffset, z + zOffset) * calcTerrainDetail(x + xOffset, z + zOffset)));

                for (int i = (int) Configuration.CHUNK_DIMENSIONS.y; i >= 0; i--) {
                    if (calcCaveDensityAt(x + xOffset, i + yOffset, z + zOffset) < 0.5) {
                        if (calcCanyonDensity(x + xOffset, i + yOffset, z + zOffset) > 0.1f) {
                            float stoneDensity = calcStoneDensity(x + xOffset, i + yOffset, z + zOffset);

                            if (i == height) {
                                // Generate grass on the top layer
                                if (i > 32) {
                                    c.setBlock(x, i, z, (byte) 0x1);
                                } else if (i <= 34 && i >= 28) {
                                    // Sand
                                    c.setBlock(x, i, z, (byte) 0x7);
                                } else {
                                    // No grass under water
                                    c.setBlock(x, i, z, (byte) 0x2);
                                }
                            } else if (i < height) {

                                // Generate beach
                                if (i <= 34 && i >= 28 && stoneDensity > 0f) {
                                    c.setBlock(x, i, z, (byte) 0x7);
                                } else if (i < height * 0.75f && stoneDensity < 0f) {
                                    // Generate the basic stone layer
                                    c.setBlock(x, i, z, (byte) 0x3);
                                } else {
                                    // Fill the upper layer with dirt
                                    c.setBlock(x, i, z, (byte) 0x2);
                                }


                                if (i <= 34 && i >= 28) {
                                    // "Beach"
                                    c.setBlock(x, i, z, (byte) 0x7);
                                }
                            }
                        }
                    }

                    if (i <= 30 && i > 0) {
                        if (c.getBlock(x, i, z) == 0) {
                            c.setBlock(x, i, z, (byte) 0x4);
                        }
                    }

                    if (i == 0) {
                        c.setBlock(x, i, z, (byte) 0x8);
                    }
                }
            }
        }
    }

    /**
     * Returns the base elevation for the terrain.
     * 
     * @param x
     * @param z
     * @return
     */
    protected float calcTerrainElevation(float x, float z) {
        float result = 0.0f;
        result += _pGen1.noiseWithOctaves(0.003f * x, 0.003f, 0.003f * z, 12) * 128f;
        return result;
    }

    /**
     * Returns the roughness for the base terrain.
     * 
     * @param x
     * @param z
     * @return
     */
    protected float calcTerrainRoughness(float x, float z) {
        float result = 0.0f;
        result += _pGen2.noiseWithOctaves(0.01f * x, 0.01f, 0.01f * z, 12);
        return result;
    }

    /**
     * Returns the detail level for the base terrain.
     * 
     * @param x
     * @param z
     * @return
     */
    protected float calcTerrainDetail(float x, float z) {
        float result = 0.0f;
        result += _pGen3.noiseWithOctaves(0.02f * x, 0.02f, 0.02f * z, 16);
        return result;
    }

    /**
     * Returns the canyon density for the base terrain.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    protected float calcCanyonDensity(float x, float y, float z) {
        float result = 0.0f;
        result += _pGen1.noiseWithOctaves(0.01f * x, 0.01f * y, 0.01f * z, 4);
        return (float) Math.abs(result);
    }

    /**
     * Returns the cave density for the base terrain.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    protected float calcCaveDensityAt(float x, float y, float z) {
        float result = 0.0f;
        result += _pGen3.noise(0.06f * x, 0.06f * y, 0.06f * z);
        return result;
    }

    /**
     * Returns the cave density for the base terrain.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    protected float calcStoneDensity(float x, float y, float z) {
        float result = 0.0f;
        result += _pGen2.noiseWithOctaves(0.1f * x, 0.1f * y, 0.1f * z, 8);
        return result;
    }
}
