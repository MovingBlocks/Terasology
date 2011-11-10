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
import com.github.begla.blockmania.utilities.MathHelper;
import com.github.begla.blockmania.world.chunk.Chunk;

/**
 * Generates the terrain of the world using a hybrid voxel-/heightmap-based approach.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorTerrain extends ChunkGenerator {

    protected static final int SAMPLE_RATE_3D_HOR = 4;
    protected static final int SAMPLE_RATE_3D_VERT = 8;

    public enum BIOME_TYPE {
        MOUNTAINS, SNOW, DESERT, FOREST, PLAINS
    }

    /**
     * Init. the generator with a given seed value.
     */
    public ChunkGeneratorTerrain(GeneratorManager generatorManager) {
        super(generatorManager);
    }

    @Override
    public void generate(Chunk c) {
        double[][][] densityMap = new double[Chunk.getChunkDimensionX() + 1][Chunk.getChunkDimensionY() + 1][Chunk.getChunkDimensionZ() + 1];

        /*
         * Create the density map at a lower sample rate.
         */
        for (int x = 0; x <= Chunk.getChunkDimensionX(); x += SAMPLE_RATE_3D_HOR) {
            for (int z = 0; z <= Chunk.getChunkDimensionZ(); z += SAMPLE_RATE_3D_HOR) {
                BIOME_TYPE type = calcBiomeTypeForGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));

                for (int y = 0; y <= Chunk.getChunkDimensionY(); y += SAMPLE_RATE_3D_VERT) {
                    densityMap[x][y][z] = calcDensity(c.getBlockWorldPosX(x), y, c.getBlockWorldPosZ(z), type);
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
        for (int x = 0; x < Chunk.getChunkDimensionX(); x++) {
            for (int z = 0; z < Chunk.getChunkDimensionZ(); z++) {
                BIOME_TYPE type = calcBiomeTypeForGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));
                int firstBlockHeight = -1;

                for (int y = Chunk.getChunkDimensionY(); y >= 0; y--) {

                    if (y == 0) { // Hard stone ground layer
                        c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Hard stone").getId());
                        break;
                    }

                    if (y <= 32 && y > 0) { // Ocean
                        c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Water").getId());

                        if (y == 32) {
                            // Ice layer
                            if (type == BIOME_TYPE.SNOW)
                                c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Ice").getId());
                        }
                    }

                    double dens = densityMap[x][y][z];

                    if ((dens >= 0 && dens < 64)) {

                        // Some block was set...
                        if (firstBlockHeight == -1)
                            firstBlockHeight = y;

                        if (calcCaveDensity(c.getBlockWorldPosX(x), y, c.getBlockWorldPosZ(z)) > -0.6)
                            GenerateOuterLayer(x, y, z, firstBlockHeight, c, type);
                        else
                            c.setBlock(x, y, z, (byte) 0x0);

                        continue;
                    } else if (dens >= 64) {

                        // Some block was set...
                        if (firstBlockHeight == -1)
                            firstBlockHeight = y;

                        if (calcCaveDensity(c.getBlockWorldPosX(x), y, c.getBlockWorldPosZ(z)) > -0.2)
                            GenerateInnerLayer(x, y, z, c, type);
                        else
                            c.setBlock(x, y, z, (byte) 0x0);

                        continue;
                    }

                    // Nothing was set!
                    firstBlockHeight = -1;
                }
            }
        }
    }

    public BIOME_TYPE calcBiomeTypeForGlobalPosition(int x, int z) {
        double temp = calcTemperatureAtGlobalPosition(x, z);
        double humidity = calcHumidityAtGlobalPosition(x, z);

        if (temp >= 0.6 && humidity < 0.3) {
            return BIOME_TYPE.DESERT;
        }
        if (temp >= 0.30 && temp < 0.6 && humidity < 0.4) {
            return BIOME_TYPE.MOUNTAINS;
        }
        if (humidity > 0.30 && humidity < 0.8 && temp > 0.5) {
            return BIOME_TYPE.PLAINS;
        }
        if (temp < 0.3 && humidity < 0.4) {
            return BIOME_TYPE.SNOW;
        }

        return BIOME_TYPE.FOREST;
    }

    protected void GenerateInnerLayer(int x, int y, int z, Chunk c, BIOME_TYPE type) {
        c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Stone").getId());
    }

    protected void GenerateOuterLayer(int x, int y, int z, int firstBlockHeight, Chunk c, BIOME_TYPE type) {

        int depth = (firstBlockHeight - y);

        switch (type) {
            case FOREST:
            case PLAINS:
            case MOUNTAINS:
                // Beach
                if (y >= 28 && y <= 34) {
                    c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Sand").getId());
                } else if (depth == 0 && y > 32) {
                    // Grass on top
                    c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Grass").getId());
                } else if (depth > 8) {
                    // Stone
                    c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Stone").getId());
                } else {
                    // Dirt
                    c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Dirt").getId());
                }

                if (type == BIOME_TYPE.PLAINS || type == BIOME_TYPE.FOREST)
                    generateRiver(c, x, y, z, depth, type);
                break;
            case SNOW:

                if (depth == 0.0 && y > 32) {
                    // Snow on top
                    c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Snow").getId());
                } else if (depth > 8) {
                    // Stone
                    c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Stone").getId());
                } else {
                    // Dirt
                    c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Dirt").getId());
                }

                generateRiver(c, x, y, z, depth, type);
                break;

            case DESERT:
                if (depth > 8) {
                    // Stone
                    c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Stone").getId());
                } else {
                    c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Sand").getId());
                }

                break;

        }
    }

    protected void generateRiver(Chunk c, int x, int y, int z, int depth, BIOME_TYPE type) {
        // Rivers under water? Nope.
        if (y < 32)
            return;

        double lakeIntens = calcLakeIntensity(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));

        if (lakeIntens < 0.15 && depth < 2) {
            c.setBlock(x, y, z, (byte) 0x0);
        } else if (lakeIntens < 0.15 && depth >= 2 && depth < 8) {
            if (type == BIOME_TYPE.SNOW) {
                c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Ice").getId());
            } else {
                c.setBlock(x, y, z, BlockManager.getInstance().getBlock("Water").getId());
            }
        }
    }

    protected void triLerpDensityMap(double[][][] densityMap) {
        for (int x = 0; x < Chunk.getChunkDimensionX(); x++) {
            for (int y = 0; y < Chunk.getChunkDimensionY(); y++) {
                for (int z = 0; z < Chunk.getChunkDimensionZ(); z++) {
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

    public double calcDensity(double x, double y, double z, BIOME_TYPE type) {
        double height = calcBaseTerrain(x, z);

        double freqY = 1.0;
        double amp = 1.0;

        if (type == BIOME_TYPE.DESERT) {
            freqY *= 1.0;
            amp *= 0.4;
        } else if (type == BIOME_TYPE.PLAINS) {
            freqY *= 1.0;
            amp *= 0.3;
        } else if (type == BIOME_TYPE.MOUNTAINS) {
            freqY *= 1.0;
            amp *= 0.8;
        } else if (type == BIOME_TYPE.SNOW) {
            freqY *= 1.0;
            amp *= 0.5;
        } else if (type == BIOME_TYPE.FOREST) {
            freqY *= 1.0;
            amp *= 0.5;
        }

        double density = calcMountainDensity(x, y * freqY, z) * amp;
        return -y + ((height * 64.0 + 32.0) + density * 128.0);
    }

    public double calcBaseTerrain(double x, double z) {
        return _pGen2.fBm(0.0009 * x, 0, 0.0009 * z, 8, 2.28371, 0.78);
    }

    public double calcMountainDensity(double x, double y, double z) {
        double x1, y1, z1;

        x1 = x * 0.008;
        y1 = y * 0.008;
        z1 = z * 0.008;

        return MathHelper.fastAbs(_pGen5.fBm(x1, y1, z1, 9, 2.163721, 0.7));
    }

    public double calcLakeIntensity(double x, double z) {
        double result = _pGen2.fBm(x * 0.004, 0, 0.004 * z, 4, 2.1836171, 0.7631);
        return Math.sqrt(MathHelper.fastAbs(result));
    }

    public double calcTemperatureAtGlobalPosition(double x, double z) {
        double result = _pGen4.fBm(x * 0.0008, 0, 0.0008 * z, 4, 2.12351, 0.91);
        return MathHelper.clamp((result + 1.0) / 2.0);
    }

    public double calcHumidityAtGlobalPosition(double x, double z) {
        double result = _pGen5.fBm(x * 0.0008, 0, 0.0008 * z, 4, 2.221312, 0.61);
        return MathHelper.clamp((result + 1.0) / 2.0);
    }

    public double calcCaveDensity(double x, double y, double z) {
        double result = _pGen6.fBm(x * 0.02, y * 0.02, z * 0.02, 3, 2.3719, 0.7);
        return result;
    }
}
