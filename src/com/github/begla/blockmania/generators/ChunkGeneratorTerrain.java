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
import com.github.begla.blockmania.utilities.MathHelper;
import com.github.begla.blockmania.world.Chunk;

/**
 * Generates the base terrain of the world.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorTerrain extends ChunkGenerator {

    /**
     *
     */
    private static final int SAMPLE_RATE_3D_HOR = 8;
    /**
     *
     */
    private static final int SAMPLE_RATE_3D_VERT = 4;

    /**
     * @param seed
     */
    public ChunkGeneratorTerrain(String seed) {
        super(seed);
    }

    /**
     * @param c
     */
    @Override
    public void generate(Chunk c) {
        float[][][] densityMap = new float[(int) Configuration.CHUNK_DIMENSIONS.x + 1][(int) Configuration.CHUNK_DIMENSIONS.y + 1][(int) Configuration.CHUNK_DIMENSIONS.z + 1];

        /*
         * Create the density map at a lower sample rate.
         */
        for (int x = 0; x <= Configuration.CHUNK_DIMENSIONS.x; x += SAMPLE_RATE_3D_HOR) {
            for (int z = 0; z <= Configuration.CHUNK_DIMENSIONS.z; z += SAMPLE_RATE_3D_HOR) {
                for (int y = 0; y <= Configuration.CHUNK_DIMENSIONS.y; y += SAMPLE_RATE_3D_VERT) {
                    densityMap[x][y][z] = calcDensity(x + getOffsetX(c), y + getOffsetY(c), z + getOffsetZ(c));
                }
            }
        }

        /*
         * Trilinear interpolate the missing values.
         */
        triLerpDensityMap(densityMap);

        /*
         * Generate the chunk from the density map.
         */
        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {

                int firstBlockHeight = -1;
                for (int y = (int) Configuration.CHUNK_DIMENSIONS.y; y >= 0; y--) {

                    if (y == 0) { // Stone ground layer
                        c.setBlock(x, y, z, (byte) 0x8);
                        break;
                    }

                    if (y < 30 && y > 0) { // Ocean
                        c.setBlock(x, y, z, (byte) 0x4);
                    }

                    float dens = densityMap[x][y][z];

                    if ((dens >= 0.01f && dens < 0.012f)) {
                        /*
                         * The outer layer is made of dirt and grass.
                         */
                        if (firstBlockHeight == -1) {
                            c.setBlock(x, y, z, getBlockTailpiece(getBlockTypeForPosition(y, 1.0f), y));

                            /* Generate lakes */
                            if (calcLakeIntensity(x + getOffsetX(c), z + getOffsetZ(c)) < 0.1) {
                                c.setBlock(x, y, z, (byte) 0x4);
                            }

                        } else {
                            c.setBlock(x, y, z, getBlockTypeForPosition(y, 1.0f - ((float) (firstBlockHeight - y) / 16f)));
                        }

                        if (firstBlockHeight == -1)
                            firstBlockHeight = y;
                    } else if (dens >= 0.012f) {
                        c.setBlock(x, y, z, getBlockTailpiece(getBlockTypeForPosition(y, 0.2f), y));

                        if (firstBlockHeight == -1)
                            firstBlockHeight = y;
                    }
                }
            }
        }
    }

    /**
     * @param densityMap
     */
    void triLerpDensityMap(float[][][] densityMap) {
        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                    if (!(x % SAMPLE_RATE_3D_HOR == 0 && y % SAMPLE_RATE_3D_VERT == 0 && z % SAMPLE_RATE_3D_HOR == 0)) {
                        int offsetX = (x / SAMPLE_RATE_3D_HOR) * SAMPLE_RATE_3D_HOR;
                        int offsetY = (y / SAMPLE_RATE_3D_VERT) * SAMPLE_RATE_3D_VERT;
                        int offsetZ = (z / SAMPLE_RATE_3D_HOR) * SAMPLE_RATE_3D_HOR;
                        densityMap[x][y][z] = MathHelper.triLerp(x, y, z, densityMap[offsetX][offsetY][offsetZ], densityMap[offsetX][SAMPLE_RATE_3D_VERT + offsetY][offsetZ], densityMap[offsetX][offsetY][offsetZ + SAMPLE_RATE_3D_HOR], densityMap[offsetX][offsetY + SAMPLE_RATE_3D_VERT][offsetZ + SAMPLE_RATE_3D_HOR], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY][offsetZ], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY + SAMPLE_RATE_3D_VERT][offsetZ], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY][offsetZ + SAMPLE_RATE_3D_HOR], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY + SAMPLE_RATE_3D_VERT][offsetZ + SAMPLE_RATE_3D_HOR], offsetX, SAMPLE_RATE_3D_HOR + offsetX, offsetY, SAMPLE_RATE_3D_VERT + offsetY, offsetZ, offsetZ + SAMPLE_RATE_3D_HOR);
                    }
                }
            }
        }
    }

    /**
     * @param type
     * @param y
     * @return
     */
    byte getBlockTailpiece(byte type, int y) {
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
     * @param y
     * @param heightPercentage
     * @return
     */
    byte getBlockTypeForPosition(int y, float heightPercentage) {
        // Sand
        if (y >= 28 && y <= 32) {
            return (byte) 0x7;
        }

        if (heightPercentage <= 0.8) {
            return (byte) 0x3;
        }

        return 0x2;
    }

    /**
     * @param x
     * @param z
     * @return
     */
    public float calcDensity(float x, float y, float z) {
        float height = (calcTerrainElevation(x, z) + 1)/2 + (calcTerrainRoughness(x, z)+1) / 2;
        float density = calcMountainDensity(x, y, z);

        density = height - density;

        if (y < 120)
            density /= (y + 1) * 1.7f;
        else
            density /= (y + 1) * 2.0f;
        return density;
    }

    /**
     * Returns the base elevation for the terrain.
     *
     * @param x
     * @param z
     * @return
     */
    float calcTerrainElevation(float x, float z) {
        float result = 0.0f;
        result += _pGen1.noise(0.0008f * x, 0.0008f * z, 0f);
        return result;
    }

    /**
     * Returns the roughness for the base terrain.
     *
     * @param x
     * @param z
     * @return
     */
    float calcTerrainRoughness(float x, float z) {
        float result = 0.0f;
        result += _pGen2.multiFractalNoise(0.001f * x, 0.00f, 0.001f * z, 7, 2.151421f);

        return result;
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return
     */
    float calcMountainDensity(float x, float y, float z) {
        float result = 0.0f;

        float x1, y1, z1;

        x1 = x * 0.0007f;
        y1 = y * 0.0009f;
        z1 = z * 0.0007f;

        float oct1 = 1.432f, oct2 = 4.4281f, oct3 = 7.371f, oct4 = 11.463819f, oct5 = 14.62819f, oct6 = 22.3672891f, oct7 = 44.47381f, oct8 = 53.47381f, oct9 = 64.47381f;

        result += _pGen2.noise(x1 * oct9, y1 * oct9, z1 * oct9) * 0.2;
        result += _pGen2.noise(x1 * oct8, y1 * oct8, z1 * oct8) * 0.3;
        result += _pGen2.noise(x1 * oct7, y1 * oct7, z1 * oct7) * 0.4;
        result += _pGen2.noise(x1 * oct6, y1 * oct6, z1 * oct6) * 0.5;
        result += _pGen2.noise(x1 * oct5, y1 * oct5, z1 * oct5) * 0.6;
        result += _pGen2.noise(x1 * oct4, y1 * oct4, z1 * oct4) * 0.7;
        result += _pGen2.noise(x1 * oct3, y1 * oct3, z1 * oct3) * 0.8;
        result += _pGen2.noise(x1 * oct2, y1 * oct2, z1 * oct2) * 0.9;
        result += _pGen2.noise(x1 * oct1, y1 * oct1, z1 * oct1) * 1.0;

        return result;
    }


    /**
     * @param x
     * @param z
     * @return
     */
    float calcLakeIntensity(float x, float z) {
        float result = 0.0f;
        result += _pGen3.multiFractalNoise(x * 0.01f, 0.01f, 0.01f * z, 8, 2.1836171f);
        return (float) Math.sqrt(Math.abs(result));
    }

}
