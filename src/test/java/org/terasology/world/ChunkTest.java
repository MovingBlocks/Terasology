package org.terasology.world;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;


public class ChunkTest {

    private Chunk chunk;

    @Before
    public void setup() {
        chunk = new Chunk(new Vector3i(0,0,0));
    }

    @Test
    public void testChangeBlock() {
        Block block = new Block();
        block.setId((byte) 4);
        BlockManager.getInstance().addBlockFamily(new SymmetricFamily(new BlockUri("some:uri"), block));
        chunk.setBlock(new Vector3i(1,2,3), block);
        assertEquals(block, chunk.getBlock(new Vector3i(1, 2, 3)));
    }

}

