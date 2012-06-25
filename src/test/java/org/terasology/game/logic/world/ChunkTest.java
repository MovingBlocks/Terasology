package org.terasology.game.logic.world;

import org.junit.Before;
import org.junit.Test;
import org.terasology.logic.world.chunks.Chunk;
import org.terasology.math.Vector3i;

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
        chunk.setBlock(new Vector3i(1,2,3), (byte) 4);
        assertEquals(4, chunk.getBlockId(new Vector3i(1, 2, 3)));
    }

}

