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
package com.github.begla.blockmania.generators;

import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.world.chunk.Chunk;
import com.github.begla.blockmania.world.simulators.LiquidSimulator;

/**
 * First draft to generate procedurally generated liquid streams. The source blocks are currently
 * determined arbitrarily – but this can be extended very easily.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorLiquids extends ChunkGeneratorTerrain {

    public ChunkGeneratorLiquids(GeneratorManager generatorManager) {
        super(generatorManager);
    }

    @Override
    public void generate(Chunk c) {
            LiquidSimulator liquidSimulator = new LiquidSimulator(_parent.getParent());


            boolean grassGenerated = false, lavaGenerated = false;
            for (int y = Chunk.CHUNK_DIMENSION_Y - 1; y >= 0; y --) {
                String title = BlockManager.getInstance().getBlock(c.getBlock(8, y, 8)).getTitle();

                boolean set = false;
                if ((title.equals("Grass") || title.equals("Snow")) && !grassGenerated && y >= 32 && _parent.getParent().getRandom().randomDouble() > 0.8) {
                    c.setBlock(8, y, 8, BlockManager.getInstance().getBlock("Water").getId());
                    set = true;
                    grassGenerated = true;
                } else if (title.equals("Stone") && !lavaGenerated && c.getBlock(8, y + 1, 8) == 0x0) {
                    c.setBlock(8, y, 8, BlockManager.getInstance().getBlock("Lava").getId());
                    set = true;
                    lavaGenerated = true;
                }

                if (set) {
                    liquidSimulator.addActiveBlock(new BlockPosition(c.getBlockWorldPosX(8), y, c.getBlockWorldPosZ(8)));

                    for (int i = 0; i < 256; i++) {
                        liquidSimulator.simulate(true);
                    }
                }

                if (lavaGenerated)
                    return;
            }
    }
}
