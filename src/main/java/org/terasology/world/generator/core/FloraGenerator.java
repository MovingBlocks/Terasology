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

import org.terasology.config.Config;
import org.terasology.config.WorldGenerationConfig;
import org.terasology.game.CoreRegistry;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.ChunkGenerator;

import java.util.Map;

/**
 * Generates some trees, flowers and high grass.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class FloraGenerator implements ChunkGenerator {

    private String worldSeed;
    private WorldBiomeProvider biomeProvider;

    private Block grassBlock;
    private Block snowBlock;
    private Block sandBlock;
    private WorldGenerationConfig config = CoreRegistry.get(Config.class).getWorldGeneration();

    public FloraGenerator() {
        grassBlock = BlockManager.getInstance().getBlock("engine:Grass");
        snowBlock = BlockManager.getInstance().getBlock("engine:Snow");
        sandBlock = BlockManager.getInstance().getBlock("engine:Sand");
    }

    @Override
    public void setWorldSeed(String seed) {
        this.worldSeed = seed;
    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
    }

    @Override
    public void generateChunk(Chunk c) {
        // TODO: Better seeding mechanism
        FastRandom random = new FastRandom(worldSeed.hashCode() ^ (c.getPos().x + 39L * (c.getPos().y + 39L * c.getPos().z)));
        for (int y = 0; y < Chunk.SIZE_Y; y++) {
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {
                    generateGrassAndFlowers(c, x, y, z, random);
                }
            }
        }
    }

    /**
     * Generates grass or a flower on the given chunk.
     *
     * @param c The chunk
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @param z Position on the z-axis
     */
    private void generateGrassAndFlowers(Chunk c, int x, int y, int z, FastRandom random) {
        Block targetBlock = c.getBlock(x, y, z);
        if ((targetBlock.equals(grassBlock) || targetBlock.equals(sandBlock) || targetBlock.equals(snowBlock)) && c.getBlock(x, y + 1, z).equals(BlockManager.getInstance().getAir())) {

            double grassRand = (random.randomDouble() + 1.0) / 2.0;
            double grassProb = 1.0;

            WorldBiomeProvider.Biome biome = biomeProvider.getBiomeAt(c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));

            switch (biome) {
                case PLAINS:
                    grassProb = 1.0 - config.getPlainsGrassDensity();
                    break;
                case MOUNTAINS:
                    grassProb = 1.0 - config.getMountainGrassDensity();
                    break;
                case FOREST:
                    grassProb = 1.0 - config.getForestGrassDensity();
                    break;
                case SNOW:
                    grassProb = 1.0 - config.getSnowGrassDensity();
                    break;
                case DESERT:
                    grassProb = 1.0 - config.getDesertGrassDensity();
                    break;
            }

            if (grassRand > grassProb) {
                /*
                 * Generate tall grass.
                 */
                double rand = random.standNormalDistrDouble();

                if (rand > -0.4 && rand < 0.4) {
                    c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:TallGrass1"));
                } else if (rand > -0.6 && rand < 0.6) {
                    c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:TallGrass2"));
                } else {
                    c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:TallGrass3"));
                }

                double flowerRand = random.randomDouble();

                /*
                 * Generate flowers.
                 */
                if (random.standNormalDistrDouble() < -2) {
                    if (flowerRand >= -1.0 && flowerRand < -0.9) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:YellowFlower"));
                    } else if (flowerRand >= -0.9 && flowerRand < -0.8) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:RedFlower"));
                    } else if (flowerRand >= -0.8 && flowerRand < -0.7) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:BrownShroom"));
                    } else if (flowerRand >= -0.7 && flowerRand < -0.6) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:BigBrownShroom"));
                    } else if (flowerRand >= -0.6 && flowerRand < -0.5) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:RedShroom"));
                    } else if (flowerRand >= -0.5 && flowerRand < -0.4) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:RedClover"));
                    } else if (flowerRand >= -0.4 && flowerRand < -0.3) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:Lavender"));
                    } else if (flowerRand >= -0.3 && flowerRand < -0.2) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:Iris"));
                    } else if (flowerRand >= -0.2 && flowerRand < -0.1) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:GlowbellBloom"));
                    } else if (flowerRand >= -0.1 && flowerRand < 0.0) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:Glowbell"));
                    } else if (flowerRand >= 0.0 && flowerRand < 0.1) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:DeadBush"));
                    } else if (flowerRand >= 0.1 && flowerRand < 0.2) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:Dandelion"));
                    } else if (flowerRand >= 0.2 && flowerRand < 0.3) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:Cotton1"));
                    } else if (flowerRand >= 0.3 && flowerRand < 0.4) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:Cotton2"));
                    } else if (flowerRand >= 0.4 && flowerRand < 0.5) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:Cotton3"));
                    } else if (flowerRand >= 0.5 && flowerRand < 0.6) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:Cotton4"));
                    } else if (flowerRand >= 0.6 && flowerRand < 0.7) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:Cotton5"));
                    } else if (flowerRand >= 0.7 && flowerRand < 0.8) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:Cotton6"));
                    } else if (flowerRand >= 0.8 && flowerRand < 0.9) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("engine:Tulip"));
                    }
                }
            }
        }
    }

    @Override
    public Map<String, String> getInitParameters() {
        return null;
    }

    @Override
    public void setInitParameters(final Map<String, String> initParameters) {
    }

}
