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
package org.terasology.logic.generators;

import org.terasology.logic.world.Chunk;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.utilities.MathHelper;

import javax.vecmath.Vector2f;

/**
 * Generates the terrain of the world using a hybrid voxel-/heightmap-based approach.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorTerrain extends ChunkGenerator {

    /* CONST */
    protected static final int SAMPLE_RATE_3D_HOR = 4;
    protected static final int SAMPLE_RATE_3D_VERT = 8;

    /**
     * Available types of biomes.
     */
    public enum BIOME_TYPE {
        MOUNTAINS, SNOW, DESERT, FOREST, PLAINS
    }

    public ChunkGeneratorTerrain(GeneratorManager generatorManager) {
        super(generatorManager);
    }

    @Override
    public void generate(Chunk c) {
        double[][][] densityMap = new double[Chunk.CHUNK_DIMENSION_X + 1][Chunk.CHUNK_DIMENSION_Y + 1][Chunk.CHUNK_DIMENSION_Z + 1];

        /*
         * Create the density map at a lower sample rate.
         */
        for (int x = 0; x <= Chunk.CHUNK_DIMENSION_X; x += SAMPLE_RATE_3D_HOR) {
            for (int z = 0; z <= Chunk.CHUNK_DIMENSION_Z; z += SAMPLE_RATE_3D_HOR) {
                for (int y = 0; y <= Chunk.CHUNK_DIMENSION_Y; y += SAMPLE_RATE_3D_VERT) {
                    densityMap[x][y][z] = calcDensity(c.getBlockWorldPosX(x), y, c.getBlockWorldPosZ(z));
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
        for (int x = 0; x < Chunk.CHUNK_DIMENSION_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_DIMENSION_Z; z++) {
                BIOME_TYPE type = calcBiomeTypeForGlobalPosition(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));
                int firstBlockHeight = -1;

                for (int y = Chunk.CHUNK_DIMENSION_Y; y >= 0; y--) {

                    if (y == 0) { // The very deepest layer of the world is an indestructible mantle
                        c.setBlock(x, y, z, BlockManager.getInstance().getBlock("MantleStone").getId());
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

                    if ((dens >= 0 && dens < 32)) {

                        // Some block was set...
                        if (firstBlockHeight == -1)
                            firstBlockHeight = y;

                        if (calcCaveDensity(c.getBlockWorldPosX(x), y, c.getBlockWorldPosZ(z)) > -0.7)
                            GenerateOuterLayer(x, y, z, firstBlockHeight, c, type);
                        else
                            c.setBlock(x, y, z, (byte) 0);

                        continue;
                    } else if (dens >= 32) {

                        // Some block was set...
                        if (firstBlockHeight == -1)
                            firstBlockHeight = y;

                        if (calcCaveDensity(c.getBlockWorldPosX(x), y, c.getBlockWorldPosZ(z)) > -0.6)
                            GenerateInnerLayer(x, y, z, c, type);
                        else
                            c.setBlock(x, y, z, (byte) 0);

                        continue;
                    }

                    // Nothing was set!
                    firstBlockHeight = -1;
                }
            }
        }
    }

    /**
     * Returns the biome type for the given global position in the world.
     *
     * @param x Position on the x-axis
     * @param z Position on the z-axis
     * @return The biome type
     */
    public BIOME_TYPE calcBiomeTypeForGlobalPosition(int x, int z) {
        double temp = calcTemperatureAtGlobalPosition(x, z);
        double humidity = calcHumidityAtGlobalPosition(x, z);

        if (temp >= 0.5 && humidity < 0.3) {
            return BIOME_TYPE.DESERT;
        } else if (humidity >= 0.3 && humidity <= 0.6 && temp >= 0.5) {
            return BIOME_TYPE.PLAINS;
        } else if (temp <= 0.3 && humidity > 0.5) {
            return BIOME_TYPE.SNOW;
        } else if (humidity >= 0.2 && humidity <= 0.6 && temp < 0.5) {
            return BIOME_TYPE.MOUNTAINS;
        }

        return BIOME_TYPE.FOREST;
    }

    protected void GenerateInnerLayer(int x, int y, int z, Chunk c, BIOME_TYPE type) {
        // TODO: GENERATE MINERALS HERE - config waiting at org\terasology\logic\manager\DefaultConfig.groovy 2012/01/22
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

    protected void triLerpDensityMap(double[][][] densityMap) {
        for (int x = 0; x < Chunk.CHUNK_DIMENSION_X; x++) {
            for (int y = 0; y < Chunk.CHUNK_DIMENSION_Y; y++) {
                for (int z = 0; z < Chunk.CHUNK_DIMENSION_Z; z++) {
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

    public double calcDensity(int x, int y, int z) {
        double height = calcBaseTerrain(x, z);
        double ocean = calcOceanTerrain(x, z);
        double river = calcRiverTerrain(x, z);

        float temp = (float) calcTemperatureAtGlobalPosition(x, z);
        float humidity = (float) calcHumidityAtGlobalPosition(x, z);

        Vector2f distanceToMountainBiome = new Vector2f(temp - 0.25f, humidity - 0.35f);

        double mIntens = MathHelper.clamp(1.0 - distanceToMountainBiome.length() * 3.0);
        double densityMountains = calcMountainDensity(x, y, z) * mIntens;
        double densityHills = calcHillDensity(x, y, z) * (1.0 - mIntens);

        int plateauArea = (int) (Chunk.CHUNK_DIMENSION_Y * 0.10);
        double flatten = MathHelper.clamp(((Chunk.CHUNK_DIMENSION_Y - 16) - y) / plateauArea);

        return -y + (((32.0 + height * 32.0) * MathHelper.clamp(river + 0.25) * MathHelper.clamp(ocean + 0.25)) + densityMountains * 1024.0 + densityHills * 128.0) * flatten;
    }

    public double calcBaseTerrain(double x, double z) {
        return MathHelper.clamp((_pGen1.fBm(0.004 * x, 0, 0.004 * z) + 1.0) / 2.0);
    }

    public double calcOceanTerrain(double x, double z) {
        return MathHelper.clamp(_pGen2.fBm(0.0009 * x, 0, 0.0009 * z) * 8.0);
    }

    public double calcRiverTerrain(double x, double z) {
        return MathHelper.clamp((Math.sqrt(Math.abs(_pGen3.fBm(0.0008 * x, 0, 0.0008 * z))) - 0.1) * 7.0);
    }

    public double calcMountainDensity(double x, double y, double z) {
        double x1, y1, z1;

        x1 = x * 0.006;
        y1 = y * 0.004;
        z1 = z * 0.006;

        double result = _pGen4.fBm(x1, y1, z1);

        return result > 0.0 ? result : 0;
    }

    public double calcHillDensity(double x, double y, double z) {
        double x1, y1, z1;

        x1 = x * 0.01;
        y1 = y * 0.008;
        z1 = z * 0.01;

        double result = _pGen5.fBm(x1, y1, z1) - 0.5;

        return result > 0.0 ? result : 0;
    }

    public double calcTemperatureAtGlobalPosition(double x, double z) {
        double result = _pGen6.fBm(x * 0.0005, 0, 0.0005 * z);
        return MathHelper.clamp((result + 1.0) / 2.0);
    }

    public double calcHumidityAtGlobalPosition(double x, double z) {
        double result = _pGen7.fBm(x * 0.0005, 0, 0.0005 * z);
        return MathHelper.clamp((result + 1.0) / 2.0);
    }

    public double calcCaveDensity(double x, double y, double z) {
        return _pGen8.fBm(x * 0.02, y * 0.02, z * 0.02);
    }
}
