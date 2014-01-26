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

package org.terasology.world;

import org.junit.Before;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.registry.CoreRegistry;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.loader.NullWorldAtlas;
import org.terasology.world.block.loader.WorldAtlasImpl;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.internal.ChunkViewCoreImpl;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class ChunkViewTest extends TerasologyTestingEnvironment {

    Block airBlock;
    Block solidBlock;

    @Before
    public void setup() throws IOException {
        BlockManagerImpl blockManager = new BlockManagerImpl(new NullWorldAtlas(), new DefaultBlockFamilyFactoryRegistry());
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
        ChunkImpl chunk = new ChunkImpl(new Vector3i());
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        ChunkViewCore chunkView = new ChunkViewCoreImpl(new ChunkImpl[]{chunk}, Region3i.createFromCenterExtents(Vector3i.zero(), Vector3i.zero()), new Vector3i());
        assertEquals(solidBlock, chunkView.getBlock(0, 0, 0));
    }

    @Test
    public void offsetWorldView() {
        ChunkImpl chunk = new ChunkImpl(new Vector3i());
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        ChunkImpl[] chunks = new ChunkImpl[]{new ChunkImpl(new Vector3i(-1, 0, -1)), new ChunkImpl(new Vector3i(0, 0, -1)), new ChunkImpl(new Vector3i(1, 0, -1)),
                new ChunkImpl(new Vector3i(-1, 0, 0)), chunk, new ChunkImpl(new Vector3i(1, 0, 0)),
                new ChunkImpl(new Vector3i(-1, 0, 1)), new ChunkImpl(new Vector3i(0, 0, 1)), new ChunkImpl(new Vector3i(1, 0, 1))};

        ChunkViewCore chunkView = new ChunkViewCoreImpl(chunks, Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1)), new Vector3i(1, 1, 1));
        assertEquals(solidBlock, chunkView.getBlock(0, 0, 0));
    }

    @Test
    public void offsetWorldViewBeforeMainChunk() {
        ChunkImpl chunk = new ChunkImpl(new Vector3i());
        chunk.setBlock(new Vector3i(15, 0, 15), solidBlock);

        ChunkImpl[] chunks = new ChunkImpl[]{chunk, new ChunkImpl(new Vector3i(0, 0, -1)), new ChunkImpl(new Vector3i(1, 0, -1)),
                new ChunkImpl(new Vector3i(-1, 0, 0)), new ChunkImpl(new Vector3i(0, 0, 0)), new ChunkImpl(new Vector3i(1, 0, 0)),
                new ChunkImpl(new Vector3i(-1, 0, 1)), new ChunkImpl(new Vector3i(0, 0, 1)), new ChunkImpl(new Vector3i(1, 0, 1))};

        ChunkViewCore chunkView = new ChunkViewCoreImpl(chunks, Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1)), new Vector3i(1, 1, 1));
        assertEquals(solidBlock, chunkView.getBlock(-1, 0, -1));
    }

    @Test
    public void offsetWorldViewAfterMainChunk() {
        ChunkImpl chunk = new ChunkImpl(new Vector3i());
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        ChunkImpl[] chunks = new ChunkImpl[]{new ChunkImpl(-1, 0, -1), new ChunkImpl(new Vector3i(0, 0, -1)), new ChunkImpl(new Vector3i(1, 0, -1)),
                new ChunkImpl(new Vector3i(-1, 0, 0)), new ChunkImpl(new Vector3i(0, 0, 0)), new ChunkImpl(new Vector3i(1, 0, 0)),
                new ChunkImpl(new Vector3i(-1, 0, 1)), new ChunkImpl(new Vector3i(0, 0, 1)), chunk};

        ChunkViewCore chunkView = new ChunkViewCoreImpl(chunks, Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1)), new Vector3i(1, 1, 1));
        assertEquals(solidBlock, chunkView.getBlock(16, 0, 16));
    }

    @Test
    public void offsetChunksWorldView() {
        ChunkImpl chunk = new ChunkImpl(new Vector3i(1, 0, 1));
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        ChunkImpl[] chunks = new ChunkImpl[]{new ChunkImpl(new Vector3i(0, 0, 0)), new ChunkImpl(new Vector3i(1, 0, 0)), new ChunkImpl(new Vector3i(2, 0, 0)),
                new ChunkImpl(new Vector3i(0, 0, 1)), chunk, new ChunkImpl(new Vector3i(2, 0, 1)),
                new ChunkImpl(new Vector3i(0, 0, 2)), new ChunkImpl(new Vector3i(1, 0, 2)), new ChunkImpl(new Vector3i(2, 0, 2))};

        ChunkViewCore chunkView = new ChunkViewCoreImpl(chunks, Region3i.createFromCenterExtents(new Vector3i(1, 0, 1), new Vector3i(1, 0, 1)), new Vector3i(1, 1, 1));
        assertEquals(solidBlock, chunkView.getBlock(0, 0, 0));
    }

    @Test
    public void localToWorld() {
        ChunkImpl chunk = new ChunkImpl(new Vector3i(1, 0, 1));
        chunk.setBlock(new Vector3i(0, 0, 0), solidBlock);

        ChunkImpl[] chunks = new ChunkImpl[]{new ChunkImpl(new Vector3i(0, 0, 0)), new ChunkImpl(new Vector3i(1, 0, 0)), new ChunkImpl(new Vector3i(2, 0, 0)),
                new ChunkImpl(new Vector3i(0, 0, 1)), chunk, new ChunkImpl(new Vector3i(2, 0, 1)),
                new ChunkImpl(new Vector3i(0, 0, 2)), new ChunkImpl(new Vector3i(1, 0, 2)), new ChunkImpl(new Vector3i(2, 0, 2))};

        ChunkViewCoreImpl chunkView = new ChunkViewCoreImpl(chunks, Region3i.createFromCenterExtents(new Vector3i(1, 0, 1), new Vector3i(1, 0, 1)), new Vector3i(1, 1, 1));
        assertEquals(new Vector3i(ChunkConstants.SIZE_X, 0, ChunkConstants.SIZE_Z), chunkView.toWorldPos(Vector3i.zero()));
    }
}
