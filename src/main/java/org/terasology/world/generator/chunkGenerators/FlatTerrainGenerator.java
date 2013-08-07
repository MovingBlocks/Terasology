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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.FirstPassGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * Create a flat world with a specified height. It use the WorldBiomeProvider
 * but not the seed.
 *
 * @author Mathias Kalb
 */
public class FlatTerrainGenerator implements FirstPassGenerator {

    private static final String INIT_PARAMETER_HEIGHT = "height";
    // TODO FlatTerrainGenerator: What is a good value for MAX_Y?
    public static final int MAX_HEIGHT = Chunk.SIZE_Y - 100;
    public static final int MIN_HEIGHT = 0;
    public static final int DEFAULT_HEIGHT = 50;

    private static final Logger logger = LoggerFactory.getLogger(FlatTerrainGenerator.class);

    private WorldBiomeProvider biomeProvider;
    private int height;

    BlockManager blockManager = CoreRegistry.get(BlockManager.class);

    private Block air = BlockManager.getAir();
    private Block mantle = blockManager.getBlock("engine:MantleStone");
    private Block stone = blockManager.getBlock("engine:Stone");
    private Block sand = blockManager.getBlock("engine:Sand");
    private Block grass = blockManager.getBlock("engine:Grass");
    private Block snow = blockManager.getBlock("engine:Snow");
    private Block dirt = blockManager.getBlock("engine:Dirt");

    public FlatTerrainGenerator() {
        height = FlatTerrainGenerator.DEFAULT_HEIGHT;
    }

    public FlatTerrainGenerator(final int height) {
        this();
        setHeight(height);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(final int height) {
        if (height < FlatTerrainGenerator.MIN_HEIGHT) {
            this.height = FlatTerrainGenerator.MIN_HEIGHT;
        } else if (height > FlatTerrainGenerator.MAX_HEIGHT) {
            this.height = FlatTerrainGenerator.MAX_HEIGHT;
        } else {
            this.height = height;
        }
    }

    public boolean isValidHeight(final int height) {
        return height >= FlatTerrainGenerator.MIN_HEIGHT && height <= FlatTerrainGenerator.MAX_HEIGHT;
    }

    /**
     * Seed is not used at the moment.
     */
    @Override
    public void setWorldSeed(final String seed) {
    }

    @Override
    public void setWorldBiomeProvider(final WorldBiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
    }

    @Override
    public Map<String, String> getInitParameters() {
        final Map<String, String> initParameters = new HashMap<String, String>();
        initParameters.put(FlatTerrainGenerator.INIT_PARAMETER_HEIGHT, String.valueOf(height));
        return initParameters;
    }

    @Override
    public void setInitParameters(final Map<String, String> initParameters) {
        if (initParameters != null) {
            final String heightStr = initParameters.get(FlatTerrainGenerator.INIT_PARAMETER_HEIGHT);
            if (heightStr != null) {
                try {
                    setHeight(Integer.parseInt(heightStr));
                } catch (final NumberFormatException e) {
                    logger.error("Cannot set initParameters to {}! ", initParameters, e);
                    // TODO Improved exception handling
                }
            }
        }
    }

    @Override
    public void generateChunk(final Chunk chunk) {
        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                final WorldBiomeProvider.Biome type = biomeProvider.getBiomeAt(chunk.getBlockWorldPosX(x), chunk.getBlockWorldPosZ(z));

                for (int y = Chunk.SIZE_Y - 1; y >= 0; y--) {
                    if (y == 0) {
                        // bedrock/mantle
                        chunk.setBlock(x, y, z, mantle);
                    } else if (y < height) {
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
                    } else if (y == height) {
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
