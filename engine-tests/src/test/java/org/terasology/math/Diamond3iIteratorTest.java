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

package org.terasology.math;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.Test;
import org.terasology.engine.math.Diamond3iIterable;

import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class Diamond3iIteratorTest {

    @Test
    public void testZeroDistanceIteration() {
        Iterator<Vector3ic> iter = Diamond3iIterable.region(new org.joml.Vector3i(),0).build().iterator();
        assertEquals(Lists.newArrayList(new Vector3i()), Lists.newArrayList(iter));
    }

    @Test
    public void testOneDistanceIteration() {
        Iterator<Vector3ic> iter = Diamond3iIterable.region(new Vector3i(),1).build().iterator();
        Set<Vector3i> expected = Sets.newHashSet(new Vector3i(), new Vector3i(1, 0, 0), new Vector3i(-1, 0, 0), new Vector3i(0, 1, 0),
            new Vector3i(0, -1, 0), new Vector3i(0, 0, 1), new Vector3i(0, 0, -1));
        while (iter.hasNext()) {
            Vector3i next = new Vector3i(iter.next());
            assertTrue(expected.remove(next), () -> "Received Unexpected: " + next);
        }
        assertTrue(expected.isEmpty(), () -> "Missing: " + expected);
    }

    @Test
    public void testTwoDistanceIteration() {
        int cc = 0;
        for (Vector3ic next : Diamond3iIterable.region(new Vector3i(), 2).build()){
            assertTrue(next.gridDistance(new Vector3i()) <= 2);
            cc++;
        }
        assertEquals(25, cc);
    }

    @Test
    public void testThreeDistanceOnlyIteration() {
        int cc = 0;
        for (Vector3ic next : Diamond3iIterable.shell(new Vector3i(), 3).build()) {
            assertEquals(3, next.gridDistance(new Vector3i()));
            cc++;
        }
        assertEquals(38, cc);
    }
}
