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
package org.terasology.logic.generators;

import org.terasology.logic.simulators.LiquidSimulator;
import org.terasology.logic.world.Chunk;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.BlockPosition;

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
        boolean grassGenerated = false, lavaGenerated = false;
        for (int y = Chunk.CHUNK_DIMENSION_Y - 1; y >= 0; y -= 2) {
            String title = BlockManager.getInstance().getBlock(c.getBlock(8, y, 8)).getTitle();
            BlockPosition blockWorldPos = new BlockPosition(c.getBlockWorldPosX(8), y, c.getBlockWorldPosZ(8));

            boolean set = false;
            if ((title.equals("Grass") || title.equals("Snow")) && !grassGenerated && y >= 32 && _parent.getParent().getRandom().randomDouble() > 0.8) {
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
                return;
        }
    }
}
