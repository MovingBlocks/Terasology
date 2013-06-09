/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.world.generator.core;

import java.util.Map;

import javax.vecmath.Vector2f;

import org.terasology.math.TeraMath;
import org.terasology.utilities.PerlinNoise;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.ChunkGenerator;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.liquid.LiquidType;

/**
 * @author Immortius
 */
public class PerlinTerrainGenerator implements ChunkGenerator {
    private static final int SAMPLE_RATE_3D_HOR = 4;
    private static final int SAMPLE_RATE_3D_VERT = 4;

    private PerlinNoise _pGen1, _pGen2, _pGen3, _pGen4, _pGen5, _pGen8;
    private WorldBiomeProvider biomeProvider;

    private Block air = BlockManager.getInstance().getAir();
    private Block mantle = BlockManager.getInstance().getBlock("engine:MantleStone");
    private Block water = BlockManager.getInstance().getBlock("engine:Water");
    private Block ice = BlockManager.getInstance().getBlock("engine:Ice");
    private Block stone = BlockManager.getInstance().getBlock("engine:Stone");
    private Block sand = BlockManager.getInstance().getBlock("engine:Sand");
    private Block grass = BlockManager.getInstance().getBlock("engine:Grass");
    private Block snow = BlockManager.getInstance().getBlock("engine:Snow");
    private Block dirt = BlockManager.getInstance().getBlock("engine:Dirt");

    @Override
    public void setWorldSeed(String seed) {
        if (seed != null) {
            _pGen1 = new PerlinNoise(seed.hashCode());
            _pGen1.setOctaves(8);

            _pGen2 = new PerlinNoise(seed.hashCode() + 1);
            _pGen2.setOctaves(8);

            _pGen3 = new PerlinNoise(seed.hashCode() + 2);
            _pGen3.setOctaves(8);

            _pGen4 = new PerlinNoise(seed.hashCode() + 3);
            _pGen5 = new PerlinNoise(seed.hashCode() + 4);
            _pGen8 = new PerlinNoise(seed.hashCode() + 7);
        }
    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
    }

    @Override
    public void generateChunk(Chunk c) {
        double[][][] densityMap = new double[Chunk.SIZE_X + 1][Chunk.SIZE_Y + 1][Chunk.SIZE_Z + 1];

        /*
         * Create the density map at a lower sample rate.
         */
        for (int x = 0; x <= Chunk.SIZE_X; x += SAMPLE_RATE_3D_HOR) {
            for (int z = 0; z <= Chunk.SIZE_Z; z += SAMPLE_RATE_3D_HOR) {
                for (int y = 0; y <= Chunk.SIZE_Y; y += SAMPLE_RATE_3D_VERT) {
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
        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                WorldBiomeProvider.Biome type = biomeProvider.getBiomeAt(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));
                int firstBlockHeight = -1;

                for (int y = Chunk.SIZE_Y-1; y >= 0; y--) {

                    if (y == 0) { // The very deepest layer of the world is an indestructible mantle
                        c.setBlock(x, y, z, mantle);
                        break;
                    }

                    if (y <= 32 && y > 0) { // Ocean
                        c.setBlock(x, y, z, water);
                        c.setLiquid(x, y, z, new LiquidData(LiquidType.WATER, Chunk.MAX_LIQUID_DEPTH));

                        if (y == 32) {
                            // Ice layer
                            if (type == WorldBiomeProvider.Biome.SNOW)
                                c.setBlock(x, y, z, ice);
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
                            c.setBlock(x, y, z, air);

                        continue;
                    } else if (dens >= 32) {

                        // Some block was set...
                        if (firstBlockHeight == -1)
                            firstBlockHeight = y;

                        if (calcCaveDensity(c.getBlockWorldPosX(x), y, c.getBlockWorldPosZ(z)) > -0.6)
                            GenerateInnerLayer(x, y, z, c, type);
                        else
                            c.setBlock(x, y, z, air);

                        continue;
                    }

                    // Nothing was set!
                    firstBlockHeight = -1;
                }
            }
        }
    }

    private void GenerateInnerLayer(int x, int y, int z, Chunk c, WorldBiomeProvider.Biome type) {
        // TODO: GENERATE MINERALS HERE - config waiting at org\terasology\logic\manager\DefaultConfig.groovy 2012/01/22
        c.setBlock(x, y, z, stone);
    }

    private void GenerateOuterLayer(int x, int y, int z, int firstBlockHeight, Chunk c, WorldBiomeProvider.Biome type) {

        int depth = (firstBlockHeight - y);

        switch (type) {
            case FOREST:
            case PLAINS:
            case MOUNTAINS:
                // Beach
                if (y >= 28 && y <= 34) {
                    c.setBlock(x, y, z, sand);
                } else if (depth == 0 && y > 32 && y < 128) {
                    // Grass on top
                    c.setBlock(x, y, z, grass);
                } else if (depth == 0 && y >= 128) {
                    // Grass on top
                    c.setBlock(x, y, z, snow);
                } else if (depth > 32) {
                    // Stone
                    c.setBlock(x, y, z, stone);
                } else {
                    // Dirt
                    c.setBlock(x, y, z, dirt);
                }

                break;
            case SNOW:
                if (depth == 0.0 && y > 32) {
                    // Snow on top
                    c.setBlock(x, y, z, snow);
                } else if (depth > 32) {
                    // Stone
                    c.setBlock(x, y, z, stone);
                } else {
                    // Dirt
                    c.setBlock(x, y, z, dirt);
                }

                break;

            case DESERT:
                if (depth > 8) {
                    // Stone
                    c.setBlock(x, y, z, stone);
                } else {
                    c.setBlock(x, y, z, sand);
                }

                break;
           case HILLS:
                if(y >= 28 && y <= 34)  {
                    c.setBlock(x,y,z,sand);
                } else if(depth == 0 && y > 32 && y < 128) {
                    c.setBlock(x,y,z,grass);
                } else if(depth <= 0 && y >= 64) {
                    c.setBlock(x,y,z, grass);
                } else if(depth <= 0 && y < 32) {
                    c.setBlock(x,y,z,stone);
                } else{
                    c.setBlock(x,y,z, dirt);
                }

                break;
        }
    }

    private void triLerpDensityMap(double[][][] densityMap) {
        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int y = 0; y < Chunk.SIZE_Y; y++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {
                    if (!(x % SAMPLE_RATE_3D_HOR == 0 && y % SAMPLE_RATE_3D_VERT == 0 && z % SAMPLE_RATE_3D_HOR == 0)) {
                        int offsetX = (x / SAMPLE_RATE_3D_HOR) * SAMPLE_RATE_3D_HOR;
                        int offsetY = (y / SAMPLE_RATE_3D_VERT) * SAMPLE_RATE_3D_VERT;
                        int offsetZ = (z / SAMPLE_RATE_3D_HOR) * SAMPLE_RATE_3D_HOR;
                        densityMap[x][y][z] = TeraMath.triLerp(x, y, z, densityMap[offsetX][offsetY][offsetZ], densityMap[offsetX][SAMPLE_RATE_3D_VERT + offsetY][offsetZ], densityMap[offsetX][offsetY][offsetZ + SAMPLE_RATE_3D_HOR], densityMap[offsetX][offsetY + SAMPLE_RATE_3D_VERT][offsetZ + SAMPLE_RATE_3D_HOR], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY][offsetZ], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY + SAMPLE_RATE_3D_VERT][offsetZ], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY][offsetZ + SAMPLE_RATE_3D_HOR], densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY + SAMPLE_RATE_3D_VERT][offsetZ + SAMPLE_RATE_3D_HOR], offsetX, SAMPLE_RATE_3D_HOR + offsetX, offsetY, SAMPLE_RATE_3D_VERT + offsetY, offsetZ, offsetZ + SAMPLE_RATE_3D_HOR);
                    }
                }
            }
        }
    }

    public double calcDensity(int x, int y, int z) {
        double height = calcBaseTerrain(x, z);
        double ocean = calcOceanTerrain(x, z);
        double river = calcRiverTerrain(x, z);

        float temp = biomeProvider.getTemperatureAt(x, z);
        float humidity = biomeProvider.getHumidityAt(x, z) * temp;

        Vector2f distanceToMountainBiome = new Vector2f(temp - 0.25f, humidity - 0.35f);

        double mIntens = TeraMath.clamp(1.0 - distanceToMountainBiome.length() * 3.0);
        double densityMountains = calcMountainDensity(x, y, z) * mIntens;
        double densityHills = calcHillDensity(x, y, z) * (1.0 - mIntens);

        int plateauArea = (int) (Chunk.SIZE_Y * 0.10);
        double flatten = TeraMath.clamp(((Chunk.SIZE_Y - 16) - y) / plateauArea);

        return -y + (((32.0 + height * 32.0) * TeraMath.clamp(river + 0.25) * TeraMath.clamp(ocean + 0.25)) + densityMountains * 1024.0 + densityHills * 128.0) * flatten;
    }

    private double calcBaseTerrain(double x, double z) {
        return TeraMath.clamp((_pGen1.fBm(0.004 * x, 0, 0.004 * z) + 1.0) / 2.0);
    }

    private double calcOceanTerrain(double x, double z) {
        return TeraMath.clamp(_pGen2.fBm(0.0009 * x, 0, 0.0009 * z) * 8.0);
    }

    private double calcRiverTerrain(double x, double z) {
        return TeraMath.clamp((java.lang.Math.sqrt(java.lang.Math.abs(_pGen3.fBm(0.0008 * x, 0, 0.0008 * z))) - 0.1) * 7.0);
    }

    private double calcMountainDensity(double x, double y, double z) {
        double x1, y1, z1;
        x1 = x * 0.002;y1 = y * 0.001; z1 = z * 0.002;

        double result = _pGen4.fBm(x1, y1, z1);
        return result > 0.0 ? result : 0;
    }

    private double calcHillDensity(double x, double y, double z) {
        double x1, y1, z1;
        x1 = x * 0.008; y1 = y * 0.006; z1 = z * 0.008;

        double result = _pGen5.fBm(x1, y1, z1) - 0.1;
        return result > 0.0 ? result : 0;
    }

    private double calcCaveDensity(double x, double y, double z) {
        return _pGen8.fBm(x * 0.02, y * 0.02, z * 0.02);
    }

    @Override
    public Map<String, String> getInitParameters() {
        return null;
    }

    @Override
    public void setInitParameters(final Map<String, String> initParameters) {
    }

}
