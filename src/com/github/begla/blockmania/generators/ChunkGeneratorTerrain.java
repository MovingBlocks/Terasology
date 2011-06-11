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

import com.github.begla.blockmania.utilities.BlockMath;
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
     */
    public static int SAMPLE_RATE_2D = 16;
    /**
     * 
     */
    public static int SAMPLE_RATE_3D_HOR = 8;
    /**
     * 
     */
    public static int SAMPLE_RATE_3D_VERT = 16;

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
        /*
         * Fetch the bilinear interpolated height map.
         */
        float[][] heightMap = generateHeightMap(c);

        /*
         * Generate the chunk from the values of the height map.
         */
        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                int height = (int) (128f * heightMap[x][z]);

                boolean first = true;
                for (int i = (int) Configuration.CHUNK_DIMENSIONS.y; i >= 0; i--) {
                    if (first && i == height) {
                        first = false;
                        // Generate grass on the top layer
                        c.setBlock(x, i, z, getBlockTailpiece(c, getBlockTypeForPosition(c, x, i, z, (float) i / (float) height), i));

                    } else if (i < height) {
                        c.setBlock(x, i, z, getBlockTypeForPosition(c, x, i, z, (float) i / (float) height));
                    }

                    /*
                     * Generate the "ocean".
                     */
                    if (i < 32 && i > 0) {
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
     * @return
     */
    protected float[][] generateHeightMap(Chunk c) {

        float[][] heightMap = new float[(int) Configuration.CHUNK_DIMENSIONS.x + 1][(int) Configuration.CHUNK_DIMENSIONS.z + 1];

        /*
         * Calculate the height map at a low sample rate.
         */
        for (int x = 0; x <= Configuration.CHUNK_DIMENSIONS.x; x += SAMPLE_RATE_2D) {
            for (int y = 0; y <= Configuration.CHUNK_DIMENSIONS.z; y += SAMPLE_RATE_2D) {
                float height = calcHeightMap(x + getOffsetX(c), y + getOffsetZ(c));
                heightMap[x][y] = height;
            }
        }

        /*
         * Binlinear interpolate the missing values.
         */
        biLerpHeightMap(heightMap);

        return heightMap;
    }

    /**
     * 
     * @param heightMap
     */
    protected void biLerpHeightMap(float[][] heightMap) {
        /*
         * Bilinear interpolate the missing values.
         */
        for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.z; y++) {
            for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
                if (!(x % SAMPLE_RATE_2D == 0 && y % SAMPLE_RATE_2D == 0)) {
                    int offsetX = (x / SAMPLE_RATE_2D) * SAMPLE_RATE_2D;
                    int offsetY = (y / SAMPLE_RATE_2D) * SAMPLE_RATE_2D;
                    heightMap[x][y] = BlockMath.biLerp(x, y, heightMap[offsetX][offsetY], heightMap[offsetX][SAMPLE_RATE_2D + offsetY], heightMap[SAMPLE_RATE_2D + offsetX][offsetY], heightMap[SAMPLE_RATE_2D + offsetX][offsetY + SAMPLE_RATE_2D], offsetX, SAMPLE_RATE_2D + offsetX, offsetY, SAMPLE_RATE_2D + offsetY);
                }
            }
        }
    }

    /**
     * 
     * @param densityMap 
     */
    protected void triLerpDensityMap(float[][][] densityMap) {
        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                    if (!(x % SAMPLE_RATE_3D_HOR == 0 && y % SAMPLE_RATE_3D_VERT == 0 && z % SAMPLE_RATE_3D_HOR == 0)) {
                        int offsetX = (x / SAMPLE_RATE_3D_HOR) * SAMPLE_RATE_3D_HOR;
                        int offsetY = (y / SAMPLE_RATE_3D_VERT) * SAMPLE_RATE_3D_VERT;
                        int offsetZ = (z / SAMPLE_RATE_3D_HOR) * SAMPLE_RATE_3D_HOR;
                        densityMap[x][y][z] = BlockMath.triLerp(x, y, z, densityMap[offsetX][offsetY][offsetZ], densityMap[offsetX][SAMPLE_RATE_3D_VERT + offsetY][offsetZ], densityMap[offsetX][offsetY][offsetZ + SAMPLE_RATE_3D_HOR], densityMap[offsetX][offsetY + SAMPLE_RATE_3D_VERT][offsetZ + SAMPLE_RATE_3D_HOR], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY][offsetZ], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY + SAMPLE_RATE_3D_VERT][offsetZ], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY][offsetZ + SAMPLE_RATE_3D_HOR], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY + SAMPLE_RATE_3D_VERT][offsetZ + SAMPLE_RATE_3D_HOR], offsetX, SAMPLE_RATE_3D_HOR + offsetX, offsetY, SAMPLE_RATE_3D_VERT + offsetY, offsetZ, offsetZ + SAMPLE_RATE_3D_HOR);
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
    public byte getBlockTypeForPosition(Chunk c, int x, int y, int z, float heightPerc) {
        // Sand
        if (y >= 28 && y <= 32) {
            return (byte) 0x7;
        }

        if (heightPerc <= 0.8) {
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
        result += _pGen1.noise(0.0009f * x, 0.0009f, 0.0009f * z) * 0.95f;
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
        result += _pGen2.multiFractalNoise(0.009f * x, 0.009f, 0.009f * z, 16, 0.25f, 2f) * 0.1f;


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
        result += _pGen3.ridgedMultiFractalNoise(x * 0.008f, 0.008f, z * 0.008f, 8, 1.2f, 3f, 1f) * 0.6;
        return result;
    }

    /**
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    protected float calcMountainDensity(float x, float y, float z) {
        float result = 0.0f;
        result += _pGen2.multiFractalNoise(x * 0.01f, 0.009f * y, z * 0.01f, 8, 8f, 12f);
        return result;
    }
}
