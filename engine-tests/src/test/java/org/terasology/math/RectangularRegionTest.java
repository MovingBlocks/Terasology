// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.math;

import org.junit.jupiter.api.Test;
import org.terasology.world.block.RectangularRegion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class RectangularRegionTest {


    @Test
    public void testContainsEmpty() {
        RectangularRegion a = new RectangularRegion(0, 0, 0, 0);
        assertTrue(a.containsPoint(0, 0));

        assertFalse(a.containsPoint(1, 0));
        assertFalse(a.containsPoint(0, 1));
        assertFalse(a.containsPoint(1, 1));
        assertFalse(a.containsPoint(-1, 0));
        assertFalse(a.containsPoint(0, -1));
        assertFalse(a.containsPoint(-1, 1));
        assertFalse(a.containsPoint(1, -1));
        assertFalse(a.containsPoint(-1, -1));
    }

    @Test
    public void testContainsPoint() {
        RectangularRegion a = new RectangularRegion(1, 2, 3, 4);
        assertTrue(a.containsPoint(1, 2));
        assertTrue(a.containsPoint(2, 3));
        assertTrue(a.containsPoint(2, 4));
        assertFalse(a.containsPoint(4, 3));
        assertFalse(a.containsPoint(2, 5));
        assertFalse(a.containsPoint(3, 5));
    }

    @Test
    public void testContainsBlockRegion() {
        RectangularRegion a = new RectangularRegion(1, 2, 10, 20);

        assertTrue(a.containsRectangularRegion(new RectangularRegion(5, 5, 5, 5)));
        assertFalse(a.containsRectangularRegion(new RectangularRegion(11, 5, 35, 5)));
        assertFalse(a.containsRectangularRegion(new RectangularRegion(1, 21, 5, 95)));

        assertTrue(a.containsRectangularRegion(new RectangularRegion(1, 2, 3, 3)));
        assertTrue(a.containsRectangularRegion(new RectangularRegion(4, 2, 8, 8)));
        assertTrue(a.containsRectangularRegion(new RectangularRegion(1, 4, 8, 8)));
        assertTrue(a.containsRectangularRegion(new RectangularRegion(5, 12, 9, 19)));
        assertTrue(a.containsRectangularRegion(new RectangularRegion(5, 12, 10, 20)));
        assertFalse(a.containsRectangularRegion(new RectangularRegion(5, 12, 10, 21)));
        assertFalse(a.containsRectangularRegion(new RectangularRegion(5, 12, 11, 20)));
    }

    @Test
    public void textExtents() {
        RectangularRegion rc = new RectangularRegion(2, 1, 10, 20);
        rc.addExtents(3, 4);
        assertEquals(-1, rc.getMinX());
        assertEquals(-3, rc.getMinY());
        assertEquals(13, rc.getMaxX());
        assertEquals(24, rc.getMaxY());
    }
}
