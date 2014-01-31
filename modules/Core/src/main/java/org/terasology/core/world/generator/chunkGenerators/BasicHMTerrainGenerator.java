/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.core.world.generator.chunkGenerators;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.procedural.HeightmapFileReader;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.FirstPassGenerator;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.liquid.LiquidType;

import java.io.IOException;
import java.util.Map;

/**
 * Generates a terrain based on a provided heightmap
 *
 * @author Nym Traveel
 */
public class BasicHMTerrainGenerator implements FirstPassGenerator {
    private static final Logger logger = LoggerFactory.getLogger(BasicHMTerrainGenerator.class);

    private WorldBiomeProvider biomeProvider;
    private Block air = BlockManager.getAir();
    private Block mantle;
    private Block water;
    private Block stone;
    private Block sand;
    private Block grass;
    private Block snow;
    private ClimateSimulator climate;
    private float[][] heightmap;

    public BasicHMTerrainGenerator() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        mantle = blockManager.getBlock("core:MantleStone");
        water = blockManager.getBlock("core:water");
        stone = blockManager.getBlock("core:Stone");
        sand = blockManager.getBlock("core:Sand");
        grass = blockManager.getBlock("core:Grass");
        snow = blockManager.getBlock("core:Snow");
    }

    @Override
    public void setWorldSeed(String seed) {
        logger.info("Initialising World"); //Why is this methode called twice?
        try {
            // TODO: Exact file to read has been hard coded in engine for security reasons until height maps become assets
            heightmap = HeightmapFileReader.readFile();
        } catch (IOException e) {
            logger.error("Failed to read heightmap", e);
        }
        heightmap = shiftArray(rotateArray(heightmap), -50, -100);
        //try also other combinations with shift and rotate
        //heightmap = rotateArray(heightmap);

        //initialize Climate/humiditymap
        //climate = new ClimateSimulator(heightmap);

    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider value) {
        this.biomeProvider = value;
    }

    @Override
    public Map<String, String> getInitParameters() {
        return null;
    }

    @Override
    public void setInitParameters(Map<String, String> initParameters) {
    }


    /**
     * Generate the local contents of a chunk. This should be purely deterministic from the chunk contents, chunk
     * position and world seed - should not depend on external state or other data.
     *
     * @param chunk
     */
    public void generateChunk(Chunk chunk) {

        int hmX = (((chunk.getChunkWorldPosX() / chunk.getChunkSizeX()) % 512) + 512) % 512;
        int hmZ = (((chunk.getChunkWorldPosZ() / chunk.getChunkSizeZ()) % 512) + 512) % 512;

        double scaleFactor = 0.05 * chunk.getChunkSizeY();

        double p00 = heightmap[hmX][hmZ] * scaleFactor;
        double p10 = heightmap[(hmX - 1 + 512) % 512][(hmZ) % 512] * scaleFactor;
        double p11 = heightmap[(hmX - 1 + 512) % 512][(hmZ + 1 + 512) % 512] * scaleFactor;
        double p01 = heightmap[(hmX) % 512][(hmZ + 1 + 512) % 512] * scaleFactor;

        for (int x = 0; x < chunk.getChunkSizeX(); x++) {
            for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                WorldBiomeProvider.Biome type = biomeProvider.getBiomeAt(
                        chunk.getBlockWorldPosX(x), chunk.getBlockWorldPosZ(z));

                //calculate avg height
                double interpolatedHeight = lerp(x / (double) chunk.getChunkSizeX(), lerp(z / (double) chunk.getChunkSizeZ(), p10, p11),
                        lerp(z / (double) chunk.getChunkSizeZ(), p00, p01));


                //Scale the height to fit one chunk (suppose we have max height 20 on the Heigthmap
                //ToDo: Change this formula in later implementation of vertical chunks
                double threshold = Math.floor(interpolatedHeight);

                for (int y = chunk.getChunkSizeY() - 1; y >= 0; y--) {
                    if (y == 0) { // The very deepest layer of the world is an
                        // indestructible mantle
                        chunk.setBlock(x, y, z, mantle);
                        break;
                    } else if (y < threshold) {
                        chunk.setBlock(x, y, z, stone);
                    } else if (y == threshold) {
                        if (y < chunk.getChunkSizeY() * 0.05 + 1) {
                            chunk.setBlock(x, y, z, sand);
                        } else if (y < chunk.getChunkSizeY() * 0.05 * 12) {
                            chunk.setBlock(x, y, z, grass);
                        } else {
                            chunk.setBlock(x, y, z, snow);
                        }
                    } else {
                        if (y <= chunk.getChunkSizeY() / 20) { // Ocean
                            chunk.setBlock(x, y, z, water);
                            chunk.setLiquid(x, y, z, new LiquidData(LiquidType.WATER,
                                    LiquidData.MAX_LIQUID_DEPTH));

                        } else {
                            chunk.setBlock(x, y, z, air);
                        }
                    }
                }
            }
        }
    }

    //helper functions for the Mapdesign until real mapGen is in
    public static float[][] rotateArray(float[][] array) {
        float[][] newArray = new float[array[0].length][array.length];
        for (int i = 0; i < newArray.length; i++) {
            for (int j = 0; j < newArray[0].length; j++) {
                newArray[i][j] = array[j][array[j].length - i - 1];
            }
        }
        return newArray;
    }

    public static float[][] shiftArray(float[][] array, int x, int y) {
        int size = array.length;
        float[][] newArray = new float[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                newArray[i][j] = array[(i + x + size) % size][(j + y + size) % size];
            }
        }
        return newArray;
    }

    private static double lerp(double t, double a, double b) {
        return a + fade(t) * (b - a);  //not sure if i should fade t, needs a bit longer to generate chunks but is definately nicer
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
}
