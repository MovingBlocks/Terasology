package org.terasology.game.logic.world;

import org.junit.Before;
import org.junit.Test;
import org.terasology.logic.newWorld.*;
import org.terasology.math.Vector3i;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ChunkTest {

    private NewChunk chunk;

    @Before
    public void setup() {
        chunk = new NewChunk(new Vector3i(0,0,0));
    }

    @Test
    public void testChangeBlock() {
        chunk.setBlock(new Vector3i(1,2,3), (byte) 4);
        assertEquals(4, chunk.getBlockId(new Vector3i(1, 2, 3)));
    }

    @Test
     public void testSetDirtyWhenOnBlockChange() {
        chunk.setDirty(false);
        assertTrue(chunk.setBlock(new Vector3i(0, 1, 2), (byte) 4));
        assertTrue(chunk.isDirty());
    }

    @Test
    public void testNoSetDirtyWhenOnBlockUnchanged() {
        chunk.setDirty(false);
        assertFalse(chunk.setBlock(new Vector3i(0, 1, 2), (byte) 0));
        assertFalse(chunk.isDirty());
    }

    @Test
    public void testSetLightDirtyWhenOnLightChange() {
        chunk.setLightDirty(false);
        assertTrue(chunk.setLight(new Vector3i(0, 1, 2), (byte) 0x3));
        assertTrue(chunk.isLightDirty());
    }

    @Test
    public void testNoSetLightDirtyWhenOnLightUnchanged() {
        chunk.setLightDirty(false);
        assertFalse(chunk.setLight(new Vector3i(0, 1, 2), (byte) 0));
        assertFalse(chunk.isLightDirty());
    }

    @Test
    public void testSetSunlightDirtyWhenOnLightChange() {
        chunk.setLightDirty(false);
        assertTrue(chunk.setSunlight(new Vector3i(0, 1, 2), (byte) 0x3));
        assertTrue(chunk.isLightDirty());
    }

    @Test
    public void testNoSetSunlightDirtyWhenOnLightUnchanged() {
        chunk.setLightDirty(false);
        assertFalse(chunk.setSunlight(new Vector3i(0, 1, 2), (byte) 0));
        assertFalse(chunk.isLightDirty());
    }

}

