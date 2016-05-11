/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.utilities.collection;

import org.junit.Test;

import static org.junit.Assert.*;

public class ListOrderedSetTest {

    @Test
    public void addItems() {
        ListOrderedSet<Integer> listOrderedSet = ListOrderedSet.create();
        for (int i = 0; i < 100; i++) {
            listOrderedSet.add(i);
            assertEquals((Integer) i, listOrderedSet.getLast());
        }
    }

    @Test
    public void removeItems() {
        ListOrderedSet<Integer> listOrderedSet = ListOrderedSet.create();
        listOrderedSet.add(1);
        listOrderedSet.add(2);
        listOrderedSet.add(3);
        listOrderedSet.add(4);
        assertEquals(4, listOrderedSet.size());

        assertTrue(listOrderedSet.remove(2));
        assertFalse(listOrderedSet.remove(0));
        assertTrue(listOrderedSet.remove(3));
        assertEquals(2, listOrderedSet.size());
        assertEquals(2, listOrderedSet.size());
        assertTrue(listOrderedSet.contains(1));
        assertTrue(listOrderedSet.contains(4));
    }
}
