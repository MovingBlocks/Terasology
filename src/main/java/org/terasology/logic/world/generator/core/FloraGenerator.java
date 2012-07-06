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
package org.terasology.logic.world.generator.core;

import org.terasology.logic.manager.Config;
import org.terasology.logic.world.chunks.Chunk;
import org.terasology.logic.world.WorldBiomeProvider;
import org.terasology.logic.world.generator.ChunkGenerator;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.utilities.FastRandom;

/**
 * Generates some trees, flowers and high grass.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class FloraGenerator implements ChunkGenerator {

    private static final double DESERT_GRASS_DENSITY = Config.getInstance().getDesertGrassDensity();
    private static final double FOREST_GRASS_DENSITY = Config.getInstance().getForestGrassDensity();
    private static final double PLAINS_GRASS_DENSITY = Config.getInstance().getPlainsGrassDensity();
    private static final double SNOW_GRASS_DENSITY = Config.getInstance().getSnowGrassDensity();
    private static final double MOUNTAINS_GRASS_DENSITY = Config.getInstance().getMountainGrassDensity();

    private String worldSeed;
    private WorldBiomeProvider biomeProvider;

    private Block grassBlock;
    private Block snowBlock;
    private Block sandBlock;

    public FloraGenerator() {
        grassBlock = BlockManager.getInstance().getBlock("Grass");
        snowBlock = BlockManager.getInstance().getBlock("Snow");
        sandBlock = BlockManager.getInstance().getBlock("Sand");
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
                    grassProb = 1.0 - PLAINS_GRASS_DENSITY;
                    break;
                case MOUNTAINS:
                    grassProb = 1.0 - MOUNTAINS_GRASS_DENSITY;
                    break;
                case FOREST:
                    grassProb = 1.0 - FOREST_GRASS_DENSITY;
                    break;
                case SNOW:
                    grassProb = 1.0 - SNOW_GRASS_DENSITY;
                    break;
                case DESERT:
                    grassProb = 1.0 - DESERT_GRASS_DENSITY;
                    break;
            }

            if (grassRand > grassProb) {
                /*
                 * Generate tall grass.
                 */
                double rand = random.standNormalDistrDouble();

                if (rand > -0.4 && rand < 0.4) {
                    c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("TallGrass1"));
                } else if (rand > -0.6 && rand < 0.6) {
                    c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("TallGrass2"));
                } else {
                    c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("TallGrass3"));
                }

                double flowerRand = random.randomDouble();

                /*
                 * Generate flowers.
                 */
                if (random.standNormalDistrDouble() < -2) {
                    if (flowerRand >= -1.0 && flowerRand < 0.2) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("RedFlower"));
                    } else if (flowerRand >= 0.2 && flowerRand < 0.6) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("YellowFlower"));
                    } else if (flowerRand >= 0.6 && flowerRand < 0.7) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("BrownShroom"));
                    } else if (flowerRand >= 0.7 && flowerRand < 0.8) {
                        c.setBlock(x, y + 1, z, BlockManager.getInstance().getBlock("RedShroom"));
                    }
                }
            }
        }
    }


}
