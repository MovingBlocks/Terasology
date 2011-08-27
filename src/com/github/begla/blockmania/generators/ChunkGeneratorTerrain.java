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
import com.github.begla.blockmania.utilities.BlockMath;
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
    private static final int SAMPLE_RATE_2D = 16;
    /**
     *
     */
    private static final int SAMPLE_RATE_3D_HOR = 8;
    /**
     *
     */
    private static final int SAMPLE_RATE_3D_VERT = 16;

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
                    densityMap[x][y][z] = calcDensity(x + getOffsetX(c),y + getOffsetY(c),z + getOffsetZ(c));
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

                boolean set = false;
                for (int y = (int) Configuration.CHUNK_DIMENSIONS.y; y >= 0; y--) {

                    if (y == 0) { // Stone ground layer
                        c.setBlock(x, y, z, (byte) 0x8);
                        break;
                    }

                    if (y < 30 && y > 0) { // Ocean
                        c.setBlock(x, y, z, (byte) 0x4);
                    }

                    float dens = densityMap[x][y][z];

                    if ((dens > 0.01f && dens < 0.1f)) {
                        /*
                         * The outer layer is made of dirt and grass.
                         */
                        if (!set) {
                            c.setBlock(x, y, z, getBlockTailpiece(c, getBlockTypeForPosition(y, 1.0f), y));
                        } else {
                            c.setBlock(x, y, z, getBlockTypeForPosition(y, 1.0f));
                        }
                        set = true;
                    } else if (dens >= 0.1f) {
                        c.setBlock(x, y, z, getBlockTailpiece(c, getBlockTypeForPosition(y, 0.2f), y));
                        set = true;
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
                        densityMap[x][y][z] = BlockMath.triLerp(x, y, z, densityMap[offsetX][offsetY][offsetZ], densityMap[offsetX][SAMPLE_RATE_3D_VERT + offsetY][offsetZ], densityMap[offsetX][offsetY][offsetZ + SAMPLE_RATE_3D_HOR], densityMap[offsetX][offsetY + SAMPLE_RATE_3D_VERT][offsetZ + SAMPLE_RATE_3D_HOR], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY][offsetZ], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY + SAMPLE_RATE_3D_VERT][offsetZ], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY][offsetZ + SAMPLE_RATE_3D_HOR], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY + SAMPLE_RATE_3D_VERT][offsetZ + SAMPLE_RATE_3D_HOR], offsetX, SAMPLE_RATE_3D_HOR + offsetX, offsetY, SAMPLE_RATE_3D_VERT + offsetY, offsetZ, offsetZ + SAMPLE_RATE_3D_HOR);
                    }
                }
            }
        }
    }

    /**
     * @param c
     * @param type
     * @param y
     * @return
     */
    byte getBlockTailpiece(Chunk c, byte type, int y) {
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
     * @param heightPerc
     * @return
     */
    byte getBlockTypeForPosition(int y, float heightPerc) {
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
     * @param x
     * @param z
     * @return
     */
    public float calcDensity(float x, float y, float z) {
        float height = (float) (calcTerrainElevation(x, z) + calcLakeIntensity(x, z) * 0.2) * 0.5f + calcTerrainRoughness(x, z) * 0.3f + calcTerrainDetail(x, z) * 0.1f;
        float density = calcMountainDensity(x, y, z);

        density = height + density;
        density /= (y + 1) * 2f;

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
        result += _pGen1.noise(0.001f * x, 0.001f, 0.001f * z);
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
        result += _pGen2.multiFractalNoise(0.0008f * x, 0.0008f, 0.0008f * z, 16, 2.351421f);

        return result;
    }

    /**
     * Returns the detail level for the base terrain.
     *
     * @param x
     * @param z
     * @return
     */
    float calcTerrainDetail(float x, float z) {
        float result = 0.0f;
        result += _pGen3.ridgedMultiFractalNoise(x * 0.004f, 0.004f, z * 0.004f, 8, 1.2f, 3f, 1f);
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

        // Turbulence
        float turb = (float) _pGen3.multiFractalNoise(x * 0.07, y * 0.07, z * 0.07, 6, 2.372618) * 32f;
        x += turb;
        y += turb;
        z += turb;

        x *= 0.0004;
        y *= 0.0005;
        z *= 0.0004;

        result += _pGen2.noise(x * 76.55, y * 76.55, z * 76.55) * 0.05;
        result += _pGen2.noise(x * 75.44, y * 75.44, z * 75.44) * 0.10;
        result += _pGen2.noise(x * 70.03, y * 70.03, z * 70.03) * 0.15;
        result += _pGen2.noise(x * 60.99, y * 60.99, z * 60.99) * 0.25;
        result += _pGen2.noise(x * 50.96, y * 50.96, z * 50.96) * 0.35;
        result += _pGen2.noise(x * 40.96, y * 40.96, z * 40.96) * 0.55;
        result += _pGen2.noise(x * 24.48, y * 24.48, z * 24.48) * 0.65;
        result += _pGen2.noise(x * 12.28, y * 8.28, z * 8.28) * 0.7;
        result += _pGen2.noise(x * 1.01, y * 1.01, z * 1.01) * 1.00;

        return result;
    }


    /**
     * @param x
     * @param z
     * @return
     */
    float calcLakeIntensity(float x, float z) {
        float result = 0.0f;
        result += _pGen3.multiFractalNoise(x * 0.01f, 0.01f, 0.01f * z, 3, 1.9836171f);
        return (float) Math.sqrt(Math.abs(result));
    }
}
