package org.terasology.logic.world;

import org.junit.Before;
import org.junit.Test;
import org.terasology.logic.world.chunks.Chunk;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ChunkTest {

    private Chunk chunk;

    @Before
    public void setup() {
        chunk = new Chunk(new Vector3i(0,0,0));
    }

    @Test
    public void testChangeBlock() {
        Block block = new Block();
        block.withId((byte)4);
        BlockManager.getInstance().addBlock(block);
        chunk.setBlock(new Vector3i(1,2,3), block);
        assertEquals(block, chunk.getBlock(new Vector3i(1, 2, 3)));
    }

}

