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
package org.terasology.core.world.liquid;

import org.terasology.world.WorldBiomeProvider;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.ChunkGenerationPass;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.liquid.LiquidType;

import java.util.Map;

/**
 * First draft to generate procedurally generated liquid streams. The source blocks are currently
 * determined arbitrarily – but this can be extended very easily.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class LiquidsGenerationPass implements ChunkGenerationPass {

    private String seed;
    private WorldBiomeProvider worldBiomeProvider;

    private Block grass;
    private Block snow;
    private Block water;
    private Block stone;
    private Block lava;

    public LiquidsGenerationPass() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        grass = blockManager.getBlock("core:Grass");
        snow = blockManager.getBlock("core:Snow");
        water = blockManager.getBlock("core:water");
        lava = blockManager.getBlock("core:lava");
        stone = blockManager.getBlock("core:Stone");
    }

    @Override
    public void generateChunk(Chunk chunk) {
        // TODO: Better seeding mechanism
        FastRandom random = new FastRandom(seed.hashCode() ^ (chunk.getPos().x + 39L * (chunk.getPos().y + 39L * chunk.getPos().z)));
        boolean grassGenerated = false;
        boolean lavaGenerated = false;
        for (int y = chunk.getChunkSizeY() - 1; y >= 0; y -= 2) {
            Block currentBlock = chunk.getBlock(8, y, 8);
            if ((grass.equals(currentBlock) || snow.equals(currentBlock)) && !grassGenerated && y >= 32 && random.nextDouble() > 0.8) {
                chunk.setBlock(8, y, 8, water);
                chunk.setLiquid(8, y, 8, new LiquidData(LiquidType.WATER, 7));
                grassGenerated = true;
            } else if ((stone.equals(currentBlock)) && !lavaGenerated && chunk.getBlock(8, y + 1, 8).equals(BlockManager.getAir())) {
                chunk.setBlock(8, y, 8, lava);
                chunk.setLiquid(8, y, 8, new LiquidData(LiquidType.LAVA, 7));
                lavaGenerated = true;
            }
        }
    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider worldBiomeProvider) {
        this.worldBiomeProvider = worldBiomeProvider;
    }

    @Override
    public void setWorldSeed(String value) {
        this.seed = value;
    }

    @Override
    public Map<String, String> getInitParameters() {
        return null;
    }

    @Override
    public void setInitParameters(final Map<String, String> initParameters) {
    }

}
