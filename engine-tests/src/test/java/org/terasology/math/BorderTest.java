/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.math;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BorderTest {

    // initialize borders for testing
    Border b0 = new Border(0, 0, 0, 0);
    Border b1 = new Border(1,1,1,1);
    Border b2 = new Border(2,2,2,2);

    @Test
    public void givenZero_whenBorder_thenEmpty() {
        assertTrue(b0.isEmpty());
    }

    @Test
    public void testBorder_whenActualBoundaries_thenResult() {
        Object b3 = new Border(1, 1,1,1);
        Border b4 = b1;
        assertEquals(b4.getLeft(), 1);
        assertEquals(b4.getRight(), 1 );
        assertEquals(b4.getBottom(), 1);
        assertEquals(b4.getTop(), 1);
        assertEquals(b4.getTotalHeight(), 2);
        assertEquals(b4.getTotalWidth(), 2);
        assertTrue(b4.equals(b1));

    }
    @Test
    public void testEquals_whenTwoObjects_thenFalse() {
        Object o = new Object();
        assertFalse(b1.equals(b2));
        assertFalse(b1.equals(o));
    }



}
