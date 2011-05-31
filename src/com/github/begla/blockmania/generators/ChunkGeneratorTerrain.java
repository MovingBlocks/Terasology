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

import com.github.begla.blockmania.world.Chunk;
import com.github.begla.blockmania.Configuration;

/**
 * Generates the base terrain of the world.
 * 
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorTerrain extends ChunkGenerator {

    /**
     *
     * @param seed
     */
    public ChunkGeneratorTerrain(String seed) {
        super(seed);
    }

    /**
     *
     * @param c
     */
    @Override
    public void generate(Chunk c) {
        int xOffset = (int) c.getPosition().x * (int) Configuration.CHUNK_DIMENSIONS.x;
        int yOffset = (int) c.getPosition().y * (int) Configuration.CHUNK_DIMENSIONS.y;
        int zOffset = (int) c.getPosition().z * (int) Configuration.CHUNK_DIMENSIONS.z;

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                int height = (int) (calcHeightMap(x + xOffset, z + zOffset) * 128f);

                if (height < 0) {
                    height = 0;
                }

                boolean first = true;
                for (int i = (int) Configuration.CHUNK_DIMENSIONS.y; i >= 0; i--) {
                    float stoneDensity = calcStoneDensity(x + xOffset, i + yOffset, z + zOffset);

                    if (first && i <= height) {
                        first = false;
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
                        } else if (i < height * 0.95f && stoneDensity < 0f) {
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
     * 
     * @param x
     * @param z
     * @return 
     */
    public float calcHeightMap(float x, float z) {
        float heightMap = (float) Math.sqrt(Math.abs(calcTerrainElevation(x, z) + (calcTerrainRoughness(x, z) * calcTerrainDetail(x, z))) / 2);
        return heightMap;
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
        result += _pGen1.noiseWithOctaves(0.0009f * x, 0.0009f, 0.0009f * z, 4, 0.5f);
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
        result += _pGen2.noiseWithOctaves(0.009f * x, 0.009f, 0.009f * z, 6, 0.5f) * 0.25f;
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
        result += _pGen3.noiseWithOctaves(0.03f * x, 0.03f, 0.03f * z, 6, 0.5f);
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
        result += _pGen2.noiseWithOctaves(0.01f * x, 0.01f * y, 0.01f * z, 8, 0.5f);
        return result;
    }
}
