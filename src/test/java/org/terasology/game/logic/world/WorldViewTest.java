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

package org.terasology.game.logic.world;

import org.junit.Before;
import org.junit.Test;
import org.terasology.logic.world.Chunk;
import org.terasology.logic.world.WorldView;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class WorldViewTest {

    Block airBlock;
    Block solidBlock;

    @Before
    public void setup() {
        airBlock = BlockManager.getInstance().getBlock((byte)0);
        solidBlock = new Block();
        solidBlock.withTitle("Stone");
        solidBlock.withId((byte)1);
        BlockManager.getInstance().addBlock(solidBlock);
    }

    @Test
     public void simpleWorldView() {
        Chunk chunk = new Chunk(new Vector3i());
        chunk.setBlock(new Vector3i(0,0,0), solidBlock);

        WorldView worldView = new WorldView(new Chunk[] {chunk}, Region3i.createFromCenterExtents(Vector3i.zero(), Vector3i.zero()), new Vector3i());
        assertEquals(solidBlock, worldView.getBlock(0, 0, 0));
    }

    @Test
    public void offsetWorldView() {
        Chunk chunk = new Chunk(new Vector3i());
        chunk.setBlock(new Vector3i(0,0,0), solidBlock);

        Chunk[] chunks = new Chunk[] {new Chunk(new Vector3i(-1,0,-1)), new Chunk(new Vector3i(0,0,-1)), new Chunk(new Vector3i(1,0,-1)),
                                            new Chunk(new Vector3i(-1,0,0)), chunk, new Chunk(new Vector3i(1,0,0)),
                                            new Chunk(new Vector3i(-1,0,1)), new Chunk(new Vector3i(0,0,1)), new Chunk(new Vector3i(1,0,1))};

        WorldView worldView = new WorldView(chunks, Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1,0,1)), new Vector3i(1,1,1));
        assertEquals(solidBlock, worldView.getBlock(0, 0, 0));
    }

    @Test
    public void offsetWorldViewBeforeMainChunk() {
        Chunk chunk = new Chunk(new Vector3i());
        chunk.setBlock(new Vector3i(15,0,15), solidBlock);

        Chunk[] chunks = new Chunk[] {chunk, new Chunk(new Vector3i(0,0,-1)), new Chunk(new Vector3i(1,0,-1)),
                new Chunk(new Vector3i(-1,0,0)), new Chunk(new Vector3i(0,0,0)), new Chunk(new Vector3i(1,0,0)),
                new Chunk(new Vector3i(-1,0,1)), new Chunk(new Vector3i(0,0,1)), new Chunk(new Vector3i(1,0,1))};

        WorldView worldView = new WorldView(chunks, Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1,0,1)), new Vector3i(1,1,1));
        assertEquals(solidBlock, worldView.getBlock(-1, 0, -1));
    }

    @Test
    public void offsetWorldViewAfterMainChunk() {
        Chunk chunk = new Chunk(new Vector3i());
        chunk.setBlock(new Vector3i(0,0,0), solidBlock);

        Chunk[] chunks = new Chunk[] {new Chunk(-1,0,-1), new Chunk(new Vector3i(0,0,-1)), new Chunk(new Vector3i(1,0,-1)),
                new Chunk(new Vector3i(-1,0,0)), new Chunk(new Vector3i(0,0,0)), new Chunk(new Vector3i(1,0,0)),
                new Chunk(new Vector3i(-1,0,1)), new Chunk(new Vector3i(0,0,1)), chunk};

        WorldView worldView = new WorldView(chunks, Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1,0,1)), new Vector3i(1,1,1));
        assertEquals(solidBlock, worldView.getBlock(16, 0, 16));
    }

    @Test
    public void offsetChunksWorldView() {
        Chunk chunk = new Chunk(new Vector3i(1,0,1));
        chunk.setBlock(new Vector3i(0,0,0), solidBlock);

        Chunk[] chunks = new Chunk[] {new Chunk(new Vector3i(0,0,0)), new Chunk(new Vector3i(1,0,0)), new Chunk(new Vector3i(2,0,0)),
                new Chunk(new Vector3i(0,0,1)), chunk, new Chunk(new Vector3i(2,0,1)),
                new Chunk(new Vector3i(0,0,2)), new Chunk(new Vector3i(1,0,2)), new Chunk(new Vector3i(2,0,2))};

        WorldView worldView = new WorldView(chunks, Region3i.createFromCenterExtents(new Vector3i(1, 0, 1), new Vector3i(1,0,1)), new Vector3i(1,1,1));
        assertEquals(solidBlock, worldView.getBlock(0, 0, 0));
    }
}
