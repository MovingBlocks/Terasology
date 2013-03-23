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
package org.terasology.world.liquid;

import org.terasology.game.CoreRegistry;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.ChunkGenerator;

import java.util.Map;

/**
 * First draft to generate procedurally generated liquid streams. The source blocks are currently
 * determined arbitrarily – but this can be extended very easily.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class LiquidsGenerator implements ChunkGenerator {

    private String seed;
    private WorldBiomeProvider biomeProvider;

    private Block grass;
    private Block snow;
    private Block water;
    private Block stone;
    private Block lava;

    public LiquidsGenerator() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        grass = blockManager.getBlock("engine:Grass");
        snow = blockManager.getBlock("engine:Snow");
        water = blockManager.getBlock("engine:Water");
        lava = blockManager.getBlock("engine:Lava");
        stone = blockManager.getBlock("engine:Stone");
    }

    @Override
    public void generateChunk(Chunk c) {
        // TODO: Better seeding mechanism
        FastRandom random = new FastRandom(seed.hashCode() ^ (c.getPos().x + 39L * (c.getPos().y + 39L * c.getPos().z)));
        boolean grassGenerated = false, lavaGenerated = false;
        for (int y = Chunk.SIZE_Y - 1; y >= 0; y -= 2) {
            Block currentBlock = c.getBlock(8, y, 8);
            if ((grass.equals(currentBlock) || snow.equals(currentBlock)) && !grassGenerated && y >= 32 && random.randomDouble() > 0.8) {
                c.setBlock(8, y, 8, water, currentBlock);
                c.setLiquid(8, y, 8, new LiquidData(LiquidType.WATER, 7));
                grassGenerated = true;
            } else if ((stone.equals(currentBlock)) && !lavaGenerated && c.getBlock(8, y + 1, 8).equals(BlockManager.getAir())) {
                c.setBlock(8, y, 8, lava, currentBlock);
                c.setLiquid(8, y, 8, new LiquidData(LiquidType.LAVA, 7));
                lavaGenerated = true;
            }
        }
    }

    @Override
    public void setWorldSeed(String seed) {
        this.seed = seed;
    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
    }

    @Override
    public Map<String, String> getInitParameters() {
        return null;
    }

    @Override
    public void setInitParameters(final Map<String, String> initParameters) {
    }

}
