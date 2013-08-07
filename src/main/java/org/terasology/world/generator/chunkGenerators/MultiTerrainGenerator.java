/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.world.generator.chunkGenerators;

import org.terasology.engine.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.EPNoise;
import org.terasology.utilities.procedural.Noise;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.FirstPassGenerator;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.liquid.LiquidType;

import javax.vecmath.Vector2f;
import java.util.Map;

/**
 * Uses multiple different methods to generate world
 *
 * @author Esa-Petri
 */
public class MultiTerrainGenerator implements FirstPassGenerator {
    private static final int SAMPLE_RATE_3D_HOR = 4;
    private static final int SAMPLE_RATE_3D_VERT = 4;

    private Noise pGen1, pGen2, pGen3, pGen4, pGen5, pGen8;
    private WorldBiomeProvider biomeProvider;

    private final Block air;
    private final Block mantle;
    private final Block water;
    private final Block ice;
    private final Block stone;
    private final Block sand;
    private final Block grass;
    private final Block snow;
    private final Block dirt;

    public MultiTerrainGenerator() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        air = BlockManager.getAir();
        mantle = blockManager.getBlock("engine:MantleStone");
        water = blockManager.getBlock("engine:Water");
        ice = blockManager.getBlock("engine:Ice");
        stone = blockManager.getBlock("engine:Stone");
        sand = blockManager.getBlock("engine:Sand");
        grass = blockManager.getBlock("engine:Grass");
        snow = blockManager.getBlock("engine:Snow");
        dirt = blockManager.getBlock("engine:Dirt");
    }

    @Override
    public void setWorldSeed(String seed) {
        if (seed != null) {
            // base
            pGen1 = new EPNoise(seed.hashCode() + 1, 1, false);
            // _pGen1 =new VornoiNoise(seed.hashCode() + 1, false, 1, 1);
            // _pGen1 = new WhiteNoise(seed.hashCode() + 1, 1);
            // _pGen1 = new DiamondSquareNoise(seed.hashCode() + 1, 3, 3);
            pGen1.setOctaves(8);

            // ocean
            // _pGen2 = new WhiteNoise(seed.hashCode() + 2, 1);
            pGen2 = new EPNoise(seed.hashCode() + 2, 6, false);
            pGen2.setOctaves(10);

            // river
            // _pGen3 = new WhiteNoise(seed.hashCode() + 3, 1);
            pGen3 = new EPNoise(seed.hashCode() + 3, 2, false);
            pGen3.setOctaves(8);

            // mountain //6 ok
            // _pGen4 = new WhiteNoise(seed.hashCode() + 4, 1);
            pGen4 = new EPNoise(seed.hashCode() + 4, 0, false);


            // hill
            // _pGen5 = new WhiteNoise(seed.hashCode() + 5, 1);
            pGen5 = new EPNoise(seed.hashCode() + 5, 2, false);

            // cave
            pGen8 = new EPNoise(seed.hashCode() + 7, 0, false);
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
                    densityMap[x][y][z] = calcDensity(c.getBlockWorldPosX(x),
                            y, c.getBlockWorldPosZ(z));
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
                WorldBiomeProvider.Biome type = biomeProvider.getBiomeAt(
                        c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));
                int firstBlockHeight = -1;

                for (int y = Chunk.SIZE_Y - 1; y >= 0; y--) {

                    if (y == 0) { // The very deepest layer of the world is an
                        // indestructible mantle
                        c.setBlock(x, y, z, mantle);
                        break;
                    }

                    if (y <= 32 && y > 0) { // Ocean
                        c.setBlock(x, y, z, water);
                        c.setLiquid(x, y, z, new LiquidData(LiquidType.WATER,
                                Chunk.MAX_LIQUID_DEPTH));

                        if (y == 32) {
                            // Ice layer
                            if (type == WorldBiomeProvider.Biome.SNOW) {
                                c.setBlock(x, y, z, ice);
                            }
                        }
                    }

                    double dens = densityMap[x][y][z];

                    if ((dens >= 0 && dens < 32)) {

                        // Some block was set...
                        if (firstBlockHeight == -1) {
                            firstBlockHeight = y;
                        }

                        if (calcCaveDensity(c.getBlockWorldPosX(x), y, c.getBlockWorldPosZ(z)) > -0.7) {
                            generateOuterLayer(x, y, z, firstBlockHeight, c, type);
                        } else {
                            c.setBlock(x, y, z, air);
                        }

                        continue;
                    } else if (dens >= 32) {

                        // Some block was set...
                        if (firstBlockHeight == -1) {
                            firstBlockHeight = y;
                        }

                        if (calcCaveDensity(c.getBlockWorldPosX(x), y,
                                c.getBlockWorldPosZ(z)) > -0.6) {
                            generateInnerLayer(x, y, z, c, type);
                        } else {
                            c.setBlock(x, y, z, air);
                        }

                        continue;
                    }

                    // Nothing was set!
                    firstBlockHeight = -1;
                }
            }
        }
    }

    private void generateInnerLayer(int x, int y, int z, Chunk c,
                                    WorldBiomeProvider.Biome type) {
        // TODO: GENERATE MINERALS HERE - config waiting at
        // org\terasology\logic\manager\DefaultConfig.groovy 2012/01/22
        c.setBlock(x, y, z, stone);
    }

    private void generateOuterLayer(int x, int y, int z, int firstBlockHeight,
                                    Chunk c, WorldBiomeProvider.Biome type) {
        // TODO Add more complicated layers
        // And we need more biomes
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
        }
    }

    private void triLerpDensityMap(double[][][] densityMap) {
        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int y = 0; y < Chunk.SIZE_Y; y++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {
                    if (!(x % SAMPLE_RATE_3D_HOR == 0
                            && y % SAMPLE_RATE_3D_VERT == 0 && z
                            % SAMPLE_RATE_3D_HOR == 0)) {
                        int offsetX = (x / SAMPLE_RATE_3D_HOR)
                                * SAMPLE_RATE_3D_HOR;
                        int offsetY = (y / SAMPLE_RATE_3D_VERT)
                                * SAMPLE_RATE_3D_VERT;
                        int offsetZ = (z / SAMPLE_RATE_3D_HOR)
                                * SAMPLE_RATE_3D_HOR;
                        densityMap[x][y][z] = TeraMath
                                .triLerp(
                                        x,
                                        y,
                                        z,
                                        densityMap[offsetX][offsetY][offsetZ],
                                        densityMap[offsetX][SAMPLE_RATE_3D_VERT
                                                + offsetY][offsetZ],
                                        densityMap[offsetX][offsetY][offsetZ
                                                + SAMPLE_RATE_3D_HOR],
                                        densityMap[offsetX][offsetY
                                                + SAMPLE_RATE_3D_VERT][offsetZ
                                                + SAMPLE_RATE_3D_HOR],
                                        densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY][offsetZ],
                                        densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY
                                                + SAMPLE_RATE_3D_VERT][offsetZ],
                                        densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY][offsetZ
                                                + SAMPLE_RATE_3D_HOR],
                                        densityMap[SAMPLE_RATE_3D_HOR + offsetX][offsetY
                                                + SAMPLE_RATE_3D_VERT][offsetZ
                                                + SAMPLE_RATE_3D_HOR], offsetX,
                                        SAMPLE_RATE_3D_HOR + offsetX, offsetY,
                                        SAMPLE_RATE_3D_VERT + offsetY, offsetZ,
                                        offsetZ + SAMPLE_RATE_3D_HOR);
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

        Vector2f distanceToMountainBiome = new Vector2f(temp - 0.25f,
                humidity - 0.35f);

        double mIntens = TeraMath
                .clamp(1.0 - distanceToMountainBiome.length() * 3.0);


        double densityMountains = calcMountainDensity(x, y, z) * mIntens;
        double densityHills = calcHillDensity(x, y, z) * (1.0 - mIntens);

        //returned to original
        int plateauArea = (int) (Chunk.SIZE_Y * 0.10);
        double flatten = TeraMath.clamp(((Chunk.SIZE_Y - 16) - y) / plateauArea);

        return -y
                + (((32.0 + height * 32.0) * TeraMath.clamp(river + 0.25) * TeraMath
                .clamp(ocean + 0.25)) + densityMountains * 1024.0 + densityHills * 128.0)
                * flatten;
    }

    private double calcBaseTerrain(double x, double z) {
        return TeraMath
                .clamp((pGen1.fBm(0.004 * x, 0, 0.004 * z) + 1.0) / 2.0);
    }

    private double calcOceanTerrain(double x, double z) {
        return TeraMath.clamp(pGen2.fBm(0.0009 * x, 0, 0.0009 * z) * 8.0);
    }

    private double calcRiverTerrain(double x, double z) {
        return TeraMath.clamp((java.lang.Math.sqrt(java.lang.Math.abs(pGen3
                .fBm(0.0008 * x, 0, 0.0008 * z))) - 0.1) * 7.0);
    }

    private double calcMountainDensity(double x, double y, double z) {
        double x1, y1, z1;
        x1 = x * 0.002;
        y1 = y * 0.001;
        z1 = z * 0.002;

        double result = pGen4.fBm(x1, y1, z1);
        return result > 0.0 ? result : 0;
    }

    private double calcHillDensity(double x, double y, double z) {
        double x1, y1, z1;
        x1 = x * 0.008;
        y1 = y * 0.006;
        z1 = z * 0.008;

        double result = pGen5.fBm(x1, y1, z1) - 0.1;
        return result > 0.0 ? result : 0;
    }

    private double calcCaveDensity(double x, double y, double z) {
        return pGen8.fBm(x * 0.02, y * 0.02, z * 0.02);
    }

    @Override
    public Map<String, String> getInitParameters() {
        return null;
    }

    @Override
    public void setInitParameters(final Map<String, String> initParameters) {
    }

}
