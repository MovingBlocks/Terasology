/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.core.config.WorldGenerationConfig;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generator.ChunkGenerationPass;

import java.util.Map;

public class OreGenerator implements ChunkGenerationPass {

    private String worldSeed;
    private WorldBiomeProvider biomeProvider;

    private Block coalBlock;
    private Block copperBlock;
    private Block ironBlock;
    private Block diamondBlock;
    private Block goldBlock;
    private Block gravelBlock;
    private Block stoneBlock;

    private WorldGenerationConfig config = new WorldGenerationConfig();

    public OreGenerator() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        coalBlock = blockManager.getBlock("core:CoalOre");
        copperBlock = blockManager.getBlock("core:CopperOre");
        ironBlock = blockManager.getBlock("core:IronOre");
        diamondBlock = blockManager.getBlock("core:DiamondOre");
        goldBlock = blockManager.getBlock("core:GoldOre");
        gravelBlock = blockManager.getBlock("core:Gravel");
        stoneBlock = blockManager.getBlock("core:Stone");
    }

    @Override
    public void setWorldSeed(String seed) {
        this.worldSeed = seed;
    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider value) {
        this.biomeProvider = value;
    }

    @Override
    public void generateChunk(CoreChunk c) {
        // TODO: Better seeding mechanism
        FastRandom random = new FastRandom(worldSeed.hashCode() ^ (c.getPosition().x + 39L * (c.getPosition().y + 39L * c.getPosition().z)));
        for (int y = 0; y < c.getChunkSizeY(); y++) {
            for (int x = 0; x < c.getChunkSizeX(); x++) {
                for (int z = 0; z < c.getChunkSizeZ(); z++) {
                    generateOre(c, x, y, z, random);
                }
            }
        }
    }

    /**
     * Generates ore on the given chunk.
     *
     * @param c The chunk
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @param z Position on the z-axis
     */
    private void generateOre(CoreChunk c, int x, int y, int z, Random random) {
        Block targetBlock = c.getBlock(x, y, z);
        if (targetBlock.equals(stoneBlock)) {
            if (random.nextFloat() < config.getDiamondDensity()) {
                c.setBlock(x, y, z, diamondBlock);
            } else if (random.nextFloat() < config.getGoldDensity()) {
                c.setBlock(x, y, z, goldBlock);
            } else if (random.nextFloat() < config.getIronDensity()) {
                c.setBlock(x, y, z, ironBlock);
            } else if (random.nextFloat() < config.getCopperDensity()) {
                c.setBlock(x, y, z, copperBlock);
            } else if (random.nextFloat() < config.getCoalDensity()) {
                c.setBlock(x, y, z, coalBlock);
            } else if (random.nextFloat() < config.getGravelDensity()) {
                c.setBlock(x, y, z, gravelBlock);
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
