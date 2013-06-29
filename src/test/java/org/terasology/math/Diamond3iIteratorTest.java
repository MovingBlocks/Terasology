/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Immortius
 */
public class Diamond3iIteratorTest {

    @Test
    public void zeroDistanceIteration() {
        Iterator<Vector3i> iter = new Diamond3iIterator(Vector3i.zero(), 0);
        assertEquals(Lists.newArrayList(Vector3i.zero()), Lists.newArrayList(iter));
    }

    @Test
    public void oneDistanceIteration() {
        Set<Vector3i> actual = Sets.newHashSet(new Diamond3iIterator(Vector3i.zero(), 1));
        Set<Vector3i> expected = Sets.newHashSet(Vector3i.zero(), new Vector3i(1,0,0), new Vector3i(-1,0,0), new Vector3i(0,1,0), new Vector3i(0,-1,0), new Vector3i(0,0,1), new Vector3i(0,0,-1));
        assertEquals(expected, actual);
    }

    @Test
    public void twoDistanceIteration() {
        Set<Vector3i> actual = Sets.newHashSet(new Diamond3iIterator(Vector3i.zero(), 2));
        assertEquals(25, actual.size());
        for (Vector3i pos : actual) {
            assertTrue(pos.gridDistance(new Vector3i()) <= 2);
        }
    }

    @Test
    public void threeDistanceOnlyIteration() {
        Set<Vector3i> actual = Sets.newHashSet(Diamond3iIterator.iterateAtDistance(new Vector3i(0,0,0), 3));
        assertEquals(38, actual.size());
        for (Vector3i pos : actual) {
            assertTrue(pos.gridDistance(new Vector3i()) == 3);
        }
    }
}
