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
import org.terasology.registry.CoreRegistry;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.generator.FirstPassGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * Create a flat world with a specified surfaceHeight. It use the WorldBiomeProvider
 * but not the seed.
 *
 * @author Mathias Kalb
 */
public class FlatTerrainGenerator implements FirstPassGenerator {

    // TODO FlatTerrainGenerator: What is a good value for MAX_Y?
    public static final int MAX_HEIGHT = ChunkConstants.SIZE_Y - 100;
    public static final int MIN_HEIGHT = 0;
    public static final int DEFAULT_HEIGHT = 50;

    private static final Logger logger = LoggerFactory.getLogger(FlatTerrainGenerator.class);
    private static final String INIT_PARAMETER_HEIGHT = "height";

    private WorldBiomeProvider biomeProvider;
    private int surfaceHeight;

    private BlockManager blockManager = CoreRegistry.get(BlockManager.class);

    private Block air = BlockManager.getAir();
    private Block mantle = blockManager.getBlock("core:MantleStone");
    private Block stone = blockManager.getBlock("core:Stone");
    private Block sand = blockManager.getBlock("core:Sand");
    private Block grass = blockManager.getBlock("core:Grass");
    private Block snow = blockManager.getBlock("core:Snow");
    private Block dirt = blockManager.getBlock("core:Dirt");

    public FlatTerrainGenerator() {
        surfaceHeight = FlatTerrainGenerator.DEFAULT_HEIGHT;
    }

    public FlatTerrainGenerator(int surfaceHeight) {
        this();
        setSurfaceHeight(surfaceHeight);
    }

    public int getSurfaceHeight() {
        return surfaceHeight;
    }

    public void setSurfaceHeight(int surfaceHeight) {
        if (surfaceHeight < FlatTerrainGenerator.MIN_HEIGHT) {
            this.surfaceHeight = FlatTerrainGenerator.MIN_HEIGHT;
        } else if (surfaceHeight > FlatTerrainGenerator.MAX_HEIGHT) {
            this.surfaceHeight = FlatTerrainGenerator.MAX_HEIGHT;
        } else {
            this.surfaceHeight = surfaceHeight;
        }
    }

    public boolean isValidHeight(int height) {
        return height >= FlatTerrainGenerator.MIN_HEIGHT && height <= FlatTerrainGenerator.MAX_HEIGHT;
    }

    /**
     * Seed is not used at the moment.
     */
    @Override
    public void setWorldSeed(String seed) {
    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider value) {
        this.biomeProvider = value;
    }

    @Override
    public Map<String, String> getInitParameters() {
        final Map<String, String> initParameters = new HashMap<String, String>();
        initParameters.put(FlatTerrainGenerator.INIT_PARAMETER_HEIGHT, String.valueOf(surfaceHeight));
        return initParameters;
    }

    @Override
    public void setInitParameters(Map<String, String> initParameters) {
        if (initParameters != null) {
            final String heightStr = initParameters.get(FlatTerrainGenerator.INIT_PARAMETER_HEIGHT);
            if (heightStr != null) {
                try {
                    setSurfaceHeight(Integer.parseInt(heightStr));
                } catch (final NumberFormatException e) {
                    logger.error("Cannot set initParameters to {}! ", initParameters, e);
                    // TODO Improved exception handling
                }
            }
        }
    }

    @Override
    public void generateChunk(final Chunk chunk) {
        for (int x = 0; x < chunk.getChunkSizeX(); x++) {
            for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                final WorldBiomeProvider.Biome type = biomeProvider.getBiomeAt(chunk.getBlockWorldPosX(x), chunk.getBlockWorldPosZ(z));

                for (int y = chunk.getChunkSizeY() - 1; y >= 0; y--) {
                    if (y == 0) {
                        // bedrock/mantle
                        chunk.setBlock(x, y, z, mantle);
                    } else if (y < surfaceHeight) {
                        // underground
                        switch (type) {
                            case FOREST:
                                chunk.setBlock(x, y, z, dirt);
                                break;
                            case PLAINS:
                                chunk.setBlock(x, y, z, dirt);
                                break;
                            case MOUNTAINS:
                                chunk.setBlock(x, y, z, stone);
                                break;
                            case SNOW:
                                chunk.setBlock(x, y, z, snow);
                                break;
                            case DESERT:
                                chunk.setBlock(x, y, z, sand);
                                break;
                        }
                    } else if (y == surfaceHeight) {
                        // surface
                        switch (type) {
                            case FOREST:
                                chunk.setBlock(x, y, z, dirt);
                                break;
                            case PLAINS:
                                chunk.setBlock(x, y, z, grass);
                                break;
                            case MOUNTAINS:
                                chunk.setBlock(x, y, z, stone);
                                break;
                            case SNOW:
                                chunk.setBlock(x, y, z, snow);
                                break;
                            case DESERT:
                                chunk.setBlock(x, y, z, sand);
                                break;
                        }
                    } else {
                        // air
                        chunk.setBlock(x, y, z, air);
                    }
                }
            }
        }
    }

}
