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
        float[][] heightMap = new float[(int) Configuration.CHUNK_DIMENSIONS.x][(int) Configuration.CHUNK_DIMENSIONS.z];

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.z; y++) {
                float height = calcHeightMap(x + getOffsetX(c), y + getOffsetZ(c));
                heightMap[x][y] = height;
            }
        }

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                int height = (int) (128 * heightMap[x][z]);

                boolean first = true;
                for (int i = (int) Configuration.CHUNK_DIMENSIONS.y; i >= 0; i--) {

                    if (first && i == height) {
                        first = false;
                        // Generate grass on the top layer
                        c.setBlock(x, i, z, getBlockTailpiece(c, getBlockTypeForPosition(c, x, i, z, height), i));

                    } else if (i < height) {
                        c.setBlock(x, i, z, getBlockTypeForPosition(c, x, i, z, height));
                    }

                    /*
                     * Generate the "ocean".
                     */
                    if (i <= 30 && i > 0) {
                        if (c.getBlock(x, i, z) == 0) {
                            c.setBlock(x, i, z, (byte) 0x4);
                        }
                    }

                    /*
                     * Generate hard stone layer.
                     */
                    if (i == 0) {
                        c.setBlock(x, i, z, (byte) 0x8);
                    }
                }
            }
        }
    }

    /**
     * 
     * @param c
     * @param type
     * @param y
     * @return
     */
    public byte getBlockTailpiece(Chunk c, byte type, int y) {
        // Sand
        if (type == 0x7) {
            return 0x7;
        } else if (type == 0x3) {
            return 0x3;
        }

        // No grass below the water surface
        if (y > 32) {
            return 0x1;
        } else {
            return 0x2;
        }
    }

    /**
     * 
     * @param c
     * @param x
     * @param y
     * @param z
     * @param height
     * @return
     */
    public byte getBlockTypeForPosition(Chunk c, int x, int y, int z, int height) {
        // Sand
        if (y <= 33 && y >= 28) {
            return (byte) 0x7;
        }

        if ((float) y / (float) height < 0.85) {
            return (byte) 0x3;
        }

        return 0x2;
    }

    /**
     * 
     * @param x
     * @param z
     * @return 
     */
    public float calcHeightMap(float x, float z) {
        float heightMap = (float) calcTerrainElevation(x, z) + (calcTerrainRoughness(x, z) * calcTerrainDetail(x, z));
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
        result += _pGen1.noise(0.0009f * x, 0.0009f, 0.0009f * z);
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
        result += _pGen3.noiseWithOctaves(0.03f * x, 0.03f, 0.03f * z, 6, 0.1f);
        return result;
    }
}
