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
import org.terasology.engine.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.loader.WorldAtlas;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.internal.ChunkImpl;

import javax.vecmath.Vector3f;

import static org.junit.Assert.assertEquals;


public class ChunkTest extends TerasologyTestingEnvironment {

    private ChunkImpl chunk;
    private BlockManagerImpl blockManager;

    @Before
    public void setup() throws Exception {
        super.setup();
        blockManager = new BlockManagerImpl(new WorldAtlas(4096), new DefaultBlockFamilyFactoryRegistry());
        CoreRegistry.put(BlockManager.class, blockManager);
        chunk = new ChunkImpl(new Vector3i(0, 0, 0));
    }

    @Test
    public void testChangeBlock() {

        blockManager.addBlockFamily(new SymmetricFamily(new BlockUri("some:uri"), new Block()), false);
        Block block = blockManager.getBlock("some:uri");
        chunk.setBlock(new Vector3i(1, 2, 3), block);
        assertEquals(block, chunk.getBlock(new Vector3i(1, 2, 3)));
    }

    @Test
    public void getAABB() {
        assertEquals(new Vector3f(0, 0, 0), chunk.getAABB().getMin());
        assertEquals(new Vector3f(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z), chunk.getAABB().getMax());
    }

}

