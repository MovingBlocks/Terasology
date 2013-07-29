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

package org.terasology.world;

import org.junit.Before;
import org.junit.Test;
import org.terasology.engine.CoreRegistry;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.loader.WorldAtlas;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.block.management.BlockManagerImpl;
import org.terasology.world.chunks.Chunk;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class ChunkViewTest {

    Block airBlock;
    Block solidBlock;

    @Before
    public void setup() {
        BlockManagerImpl blockManager = new BlockManagerImpl(new WorldAtlas(4096), new DefaultBlockFamilyFactoryRegistry());
        CoreRegistry.put(BlockManager.class, blockManager);
        airBlock = BlockManager.getAir();
        solidBlock = new Block();
        solidBlock.setDisplayName("Stone");
        solidBlock.setUri(new BlockUri("engine:stone"));
        blockManager.addBlockFamily(new SymmetricFamily(solidBlock.getURI(), solidBlock), true);
        solidBlock = blockManager.getBlock(solidBlock.getURI());
    }

    @Test
    public void simpleWorldView() {
        Chunk chunk = new Chunk(new Vector3i());
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        ChunkView chunkView = new RegionalChunkView(new Chunk[]{chunk}, Region3i.createFromCenterExtents(Vector3i.zero(), Vector3i.zero()), new Vector3i());
        assertEquals(solidBlock, chunkView.getBlock(0, 0, 0));
    }

    @Test
    public void offsetWorldView() {
        Chunk chunk = new Chunk(new Vector3i());
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        Chunk[] chunks = new Chunk[]{new Chunk(new Vector3i(-1, 0, -1)), new Chunk(new Vector3i(0, 0, -1)), new Chunk(new Vector3i(1, 0, -1)),
                new Chunk(new Vector3i(-1, 0, 0)), chunk, new Chunk(new Vector3i(1, 0, 0)),
                new Chunk(new Vector3i(-1, 0, 1)), new Chunk(new Vector3i(0, 0, 1)), new Chunk(new Vector3i(1, 0, 1))};

        ChunkView chunkView = new RegionalChunkView(chunks, Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1)), new Vector3i(1, 1, 1));
        assertEquals(solidBlock, chunkView.getBlock(0, 0, 0));
    }

    @Test
    public void offsetWorldViewBeforeMainChunk() {
        Chunk chunk = new Chunk(new Vector3i());
        chunk.setBlock(new Vector3i(15, 0, 15), solidBlock);

        Chunk[] chunks = new Chunk[]{chunk, new Chunk(new Vector3i(0, 0, -1)), new Chunk(new Vector3i(1, 0, -1)),
                new Chunk(new Vector3i(-1, 0, 0)), new Chunk(new Vector3i(0, 0, 0)), new Chunk(new Vector3i(1, 0, 0)),
                new Chunk(new Vector3i(-1, 0, 1)), new Chunk(new Vector3i(0, 0, 1)), new Chunk(new Vector3i(1, 0, 1))};

        ChunkView chunkView = new RegionalChunkView(chunks, Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1)), new Vector3i(1, 1, 1));
        assertEquals(solidBlock, chunkView.getBlock(-1, 0, -1));
    }

    @Test
    public void offsetWorldViewAfterMainChunk() {
        Chunk chunk = new Chunk(new Vector3i());
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        Chunk[] chunks = new Chunk[]{new Chunk(-1, 0, -1), new Chunk(new Vector3i(0, 0, -1)), new Chunk(new Vector3i(1, 0, -1)),
                new Chunk(new Vector3i(-1, 0, 0)), new Chunk(new Vector3i(0, 0, 0)), new Chunk(new Vector3i(1, 0, 0)),
                new Chunk(new Vector3i(-1, 0, 1)), new Chunk(new Vector3i(0, 0, 1)), chunk};

        ChunkView chunkView = new RegionalChunkView(chunks, Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1)), new Vector3i(1, 1, 1));
        assertEquals(solidBlock, chunkView.getBlock(16, 0, 16));
    }

    @Test
    public void offsetChunksWorldView() {
        Chunk chunk = new Chunk(new Vector3i(1, 0, 1));
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        Chunk[] chunks = new Chunk[]{new Chunk(new Vector3i(0, 0, 0)), new Chunk(new Vector3i(1, 0, 0)), new Chunk(new Vector3i(2, 0, 0)),
                new Chunk(new Vector3i(0, 0, 1)), chunk, new Chunk(new Vector3i(2, 0, 1)),
                new Chunk(new Vector3i(0, 0, 2)), new Chunk(new Vector3i(1, 0, 2)), new Chunk(new Vector3i(2, 0, 2))};

        ChunkView chunkView = new RegionalChunkView(chunks, Region3i.createFromCenterExtents(new Vector3i(1, 0, 1), new Vector3i(1, 0, 1)), new Vector3i(1, 1, 1));
        assertEquals(solidBlock, chunkView.getBlock(0, 0, 0));
    }

    @Test
    public void localToWorld() {
        Chunk chunk = new Chunk(new Vector3i(1, 0, 1));
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        Chunk[] chunks = new Chunk[]{new Chunk(new Vector3i(0, 0, 0)), new Chunk(new Vector3i(1, 0, 0)), new Chunk(new Vector3i(2, 0, 0)),
                new Chunk(new Vector3i(0, 0, 1)), chunk, new Chunk(new Vector3i(2, 0, 1)),
                new Chunk(new Vector3i(0, 0, 2)), new Chunk(new Vector3i(1, 0, 2)), new Chunk(new Vector3i(2, 0, 2))};

        RegionalChunkView chunkView = new RegionalChunkView(chunks, Region3i.createFromCenterExtents(new Vector3i(1, 0, 1), new Vector3i(1, 0, 1)), new Vector3i(1, 1, 1));
        assertEquals(new Vector3i(Chunk.SIZE_X, 0, Chunk.SIZE_Z), chunkView.toWorldPos(Vector3i.zero()));
    }
}
