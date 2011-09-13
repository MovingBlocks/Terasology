package com.github.begla.blockmania.tests;


import com.github.begla.blockmania.world.chunk.Chunk;
import org.junit.Test;
import org.lwjgl.util.vector.Vector3f;

public class BlockmaniaChunkTest extends junit.framework.TestCase {


    @Test
    public void testChunkToWorldMapping() throws Exception {
        Chunk chunk1 = new Chunk(null, new Vector3f(-1, 0, -1), null);

        int blockPosX = 0;
        int blockPosZ = 0;

        assertEquals(-16, chunk1.getBlockWorldPosX(blockPosX));
        assertEquals(-16, chunk1.getBlockWorldPosZ(blockPosZ));

        blockPosX = 15;
        blockPosZ = 15;

        assertEquals(-1, chunk1.getBlockWorldPosX(blockPosX));
        assertEquals(-1, chunk1.getBlockWorldPosZ(blockPosZ));

        blockPosX = 15;
        blockPosZ = 15;

        assertEquals(-1, chunk1.getBlockWorldPosX(blockPosX));
        assertEquals(-1, chunk1.getBlockWorldPosZ(blockPosZ));

        Chunk chunk2 = new Chunk(null, new Vector3f(-2, 0, -2), null);

        blockPosX = 15;
        blockPosZ = 15;

        assertEquals(-17, chunk2.getBlockWorldPosX(blockPosX));
        assertEquals(-17, chunk2.getBlockWorldPosZ(blockPosZ));
    }

}
