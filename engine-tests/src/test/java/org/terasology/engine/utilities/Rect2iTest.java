// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities;


import org.junit.jupiter.api.Test;
import org.terasology.math.geom.Rect2i;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Rect2iTest {

    @Test
    public void testOverlap() {
        assertTrue(Rect2i.createFromMinAndSize(5, 5, 472, 17).overlaps(Rect2i.createFromMinAndSize(5, 5, 1, 16)));
    }

    @Test
    public void testEncompass() {
        // encompass self
        assertTrue(Rect2i.createFromMinAndSize(5, 5, 47, 57).contains(Rect2i.createFromMinAndSize(5, 5, 47, 57)));

        assertTrue(Rect2i.createFromMinAndSize(5, 5, 47, 57).contains(Rect2i.createFromMinAndSize(45, 35, 5, 20)));
        assertTrue(Rect2i.createFromMinAndSize(5, 5, 47, 57).contains(Rect2i.createFromMinAndSize(50, 60, 2, 2)));

        assertFalse(Rect2i.createFromMinAndSize(5, 5, 47, 57).contains(Rect2i.createFromMinAndSize(50, 60, 3, 2)));
        assertFalse(Rect2i.createFromMinAndSize(5, 5, 47, 57).contains(Rect2i.createFromMinAndSize(50, 60, 2, 3)));
    }

    @Test
    public void testContains() {
        Rect2i a = Rect2i.createFromMinAndMax(0, 0, 0, 0);
        assertTrue(a.contains(0, 0));

        assertFalse(a.contains(1, 0));
        assertFalse(a.contains(0, 1));
        assertFalse(a.contains(1, 1));
        assertFalse(a.contains(-1, 0));
        assertFalse(a.contains(0, -1));
        assertFalse(a.contains(-1, 1));
        assertFalse(a.contains(1, -1));
        assertFalse(a.contains(-1, -1));
    }

}
