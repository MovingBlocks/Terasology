package org.terasology.pathfinding.model;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author synopia
 */
public class BitMapTest {
    @Test
    public void testNoOverlap() {
        BitMap r1 = new BitMap();
        BitMap r2 = new BitMap();

        r1.setPassable(0, 0);
        r1.setPassable(1, 0);
        r1.setPassable(2, 0);

        r2.setPassable(0, 1);
        r2.setPassable(1, 1);
        r2.setPassable(2, 1);

        Assert.assertFalse( r1.overlap(r2) );
        Assert.assertFalse( r2.overlap(r1) );
    }
    @Test
    public void testOverlap() {
        BitMap r1 = new BitMap();
        BitMap r2 = new BitMap();

        r1.setPassable(0, 0);
        r1.setPassable(1, 0);
        r1.setPassable(2, 0);
        r2.setPassable(0, 1);
        r2.setPassable(1, 0);
        r2.setPassable(1, 1);
        r2.setPassable(2, 1);

        Assert.assertTrue(r1.overlap(r2));
        Assert.assertTrue(r2.overlap(r1));
    }
}
