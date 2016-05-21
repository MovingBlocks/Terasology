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

public class CircularToggleSetTest {

    @Test
    public void testAdd() {
        CircularToggleSet<Character> set = new CircularToggleSet<>();

        String abcd = "ABCD";
        for (int i = 0; i < abcd.length(); i++) {
            Character c = abcd.charAt(i);
            set.add(c);
        }

        for (int i = 0; i < abcd.length(); i++) {
            Character c = abcd.charAt(i);
            assertEquals(c, set.toggle());
        }

        for (int i = 0; i < abcd.length(); i++) {
            Character c = abcd.charAt(i);
            assertEquals(c, set.toggle());
        }
    }


    @Test
    public void testRemove0() {
        CircularToggleSet<Character> set = new CircularToggleSet<>();
        assertNull(set.toggle());
        assertEquals(0, set.size());
        assertTrue(set.add('B'));
        assertEquals(1, set.size());
        assertEquals((Character) 'B', set.toggle());
        assertEquals((Character) 'B', set.toggle());
        assertTrue(set.remove('B'));
        assertNull(set.toggle());
        assertTrue(set.add('B'));
        assertEquals((Character) 'B', set.toggle());
        assertEquals((Character) 'B', set.toggle());
    }

    @Test
    public void testRemove1() {
        CircularToggleSet<Character> set = new CircularToggleSet<>();

        String abcd = "ABCD";
        for (int i = 0; i < abcd.length(); i++) {
            Character c = abcd.charAt(i);
            assertTrue(set.add(c));
        }
        set.toggle();
        assertEquals((Character) 'B', set.toggle());
        set.remove('C');
        assertEquals((Character) 'D', set.toggle());
    }

    @Test
    public void testRemove2() {
        CircularToggleSet<Character> set = new CircularToggleSet<>();

        String abcd = "ABCDEFG";
        for (int i = 0; i < abcd.length(); i++) {
            Character c = abcd.charAt(i);
            assertTrue(set.add(c));
        }
        assertEquals((Character) 'A', set.toggle());

        // Removing [B, F]
        for (int i = 1; i < abcd.length() - 1; i++) {
            Character c = abcd.charAt(i);
            assertTrue(set.remove(c));
        }
        assertEquals((Character) 'G', set.toggle());
    }

    @Test
    public void testRemove3() {
        CircularToggleSet<Character> set = new CircularToggleSet<>();

        String abcd = "ABC";
        for (int i = 0; i < abcd.length(); i++) {
            Character c = abcd.charAt(i);
            assertTrue(set.add(c));
        }
        assertTrue(set.remove('A'));
        assertEquals((Character) 'B', set.toggle());

    }

    @Test
    public void testRemove4() {
        CircularToggleSet<Character> set = new CircularToggleSet<>();

        String abcd = "ABC";
        for (int i = 0; i < abcd.length(); i++) {
            Character c = abcd.charAt(i);
            assertTrue(set.add(c));
        }
        assertTrue(set.remove('A'));
        assertTrue(set.remove('B'));
        assertEquals((Character) 'C', set.toggle());
    }

    @Test
    public void testRemove5() {
        CircularToggleSet<Character> set = new CircularToggleSet<>();

        String abcd = "ABC";
        for (int i = 0; i < abcd.length(); i++) {
            Character c = abcd.charAt(i);
            assertTrue(set.add(c));
        }

        assertTrue(set.remove('B'));
        assertTrue(set.remove('C'));
        assertEquals((Character) 'A', set.toggle());
        assertEquals((Character) 'A', set.toggle());
        assertEquals((Character) 'A', set.toggle());

    }

    @Test
    public void testRemove6() {
        CircularToggleSet<Character> set = new CircularToggleSet<>();

        String abcd = "ABC";
        for (int i = 0; i < abcd.length(); i++) {
            Character c = abcd.charAt(i);
            assertTrue(set.add(c));
        }

        assertTrue(set.remove('A'));
        assertTrue(set.remove('B'));
        assertEquals((Character) 'C', set.toggle());
        assertEquals((Character) 'C', set.toggle());
        assertEquals((Character) 'C', set.toggle());

    }

    @Test
    public void testClear() {
        CircularToggleSet<Character> set = new CircularToggleSet<>();

        String abcd = "ABC";
        for (int i = 0; i < abcd.length(); i++) {
            Character c = abcd.charAt(i);
            set.add(c);
        }
        set.clear();
        String def = "DEF";
        for (int i = 0; i < def.length(); i++) {
            Character c = def.charAt(i);
            set.add(c);
        }
        assertEquals((Character) 'D', set.toggle());
        assertEquals((Character) 'E', set.toggle());
        assertEquals((Character) 'F', set.toggle());
        assertEquals((Character) 'D', set.toggle());

    }

    @Test
    public void testNullBehaviour0() {
        CircularToggleSet<Character> set = new CircularToggleSet<>();
        assertNull(set.toggle());
    }


    @Test
    public void testNullBehaviour1() {
        CircularToggleSet<Character> set = new CircularToggleSet<>();
        set.add('A');
        assertEquals((Character) 'A', set.toggle());
        set.remove('A');
        assertNull(set.toggle());
    }
}
