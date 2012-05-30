/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.logic.world.generator;

import org.terasology.logic.world.Chunk;
import org.terasology.logic.world.ChunkGenerator;
import org.terasology.logic.world.WorldBiomeProvider;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.utilities.FastRandom;

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
        grass = BlockManager.getInstance().getBlock("Grass");
        snow = BlockManager.getInstance().getBlock("Snow");
        water = BlockManager.getInstance().getBlock("Water");
        lava = BlockManager.getInstance().getBlock("Lava");
        stone = BlockManager.getInstance().getBlock("Stone");
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
                grassGenerated = true;
            } else if ((stone.equals(currentBlock)) && !lavaGenerated && c.getBlock(8, y + 1, 8).equals(BlockManager.getInstance().getAir())) {
                c.setBlock(8, y, 8, lava, currentBlock);
                lavaGenerated = true;
            }

            // TODO: Liquid simulator notification? Or leave that to full chunk completion notification?

            /*boolean set = false;
            if ((title.equals("Grass") || title.equals("Snow")) && !grassGenerated && y >= 32 && random.randomDouble() > 0.8) {
                _parent.getParent().setBlock(blockWorldPos.x, blockWorldPos.y, blockWorldPos.z, BlockManager.getInstance().getBlock("Water").getId(), true, true, true);
                set = true;
                grassGenerated = true;
            } else if (title.equals("Stone") && !lavaGenerated && c.getBlock(8, y + 1, 8) == 0x0) {
                _parent.getParent().setBlock(blockWorldPos.x, blockWorldPos.y, blockWorldPos.z, BlockManager.getInstance().getBlock("Lava").getId(), true, true, true);
                set = true;
                lavaGenerated = true;
            }

            if (set) {
                LiquidSimulator sim = new LiquidSimulator(_parent.getParent());
                sim.addActiveBlock(blockWorldPos);
                sim.simulateAll();
            }

            if (lavaGenerated)
                return;*/
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
}
