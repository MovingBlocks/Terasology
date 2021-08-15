// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.utilities.collection;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.engine.utilities.collection.CircularBuffer;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CircularBufferTest {

    @Test
    public void testAddItems() {
        CircularBuffer<Integer> buffer = CircularBuffer.create(4);
        for (int i = 0; i < 100; ++i) {
            buffer.add(i);
            assertEquals((Integer) i, buffer.getLast());
        }
    }

    @Test
    public void testRemoveItems() {
        CircularBuffer<Integer> buffer = CircularBuffer.create(4);
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);
        buffer.add(4);
        buffer.add(5);
        assertEquals(4, buffer.size());
        assertEquals((Integer) 2, buffer.getFirst());
        assertEquals((Integer) 2, buffer.popFirst());
        assertEquals((Integer) 3, buffer.getFirst());
        assertEquals(3, buffer.size());
        assertEquals((Integer) 5, buffer.popLast());
        assertEquals((Integer) 4, buffer.popLast());
        assertEquals((Integer) 3, buffer.getLast());
        assertEquals((Integer) 3, buffer.popLast());
        assertTrue(buffer.isEmpty());
    }

    @Test
    public void testCollectionMethods() {

        Collection<Integer> buffer = CircularBuffer.create(4);

        buffer.addAll(ImmutableList.of(1, 2, 3, 4, 5, 6));
        buffer.add(4);

        assertTrue(buffer.contains(5));
        assertTrue(buffer.containsAll(ImmutableList.of(5, 4)));
    }

    @Test
    public void testGetSet() {

        CircularBuffer<Integer> buffer = CircularBuffer.create(4);

        buffer.addAll(ImmutableList.of(11, 12, 0, 1, 2, 3));

        assertEquals((Integer) 0, buffer.get(0));
        assertEquals((Integer) 3, buffer.get(3));
        assertEquals((Integer) 2, buffer.set(2, 8));
        assertEquals((Integer) 8, buffer.get(2));

        assertEquals((Integer) 0, buffer.set(0, 5));
        assertEquals((Integer) 5, buffer.get(0));

        assertEquals((Integer) 3, buffer.set(3, 6));
        assertEquals((Integer) 6, buffer.get(3));

    }

    @Test
    public void testInsert() {
        CircularBuffer<Integer> buffer = CircularBuffer.create(4);
        buffer.addAll(ImmutableList.of(1, 2, 5, 7));

        // remove from the middle
        assertEquals((Integer) 2, buffer.remove(1));
        assertEquals((Integer) 5, buffer.get(1));

        // remove from the left side
        assertEquals((Integer) 1, buffer.remove(0));

        // remove from the right side
        assertEquals((Integer) 7, buffer.remove(1));

        // remove the only element
        assertEquals((Integer) 5, buffer.remove(0));

        assertTrue(buffer.isEmpty());
    }

    @Test
    public void testIterator1() {
        CircularBuffer<Integer> buffer = CircularBuffer.create(2);
        buffer.addAll(ImmutableList.of(1, 2));
        Iterator<Integer> iterator = buffer.iterator();
        iterator.next();
        iterator.remove();
    }

    @Test
    public void testIterator2() {
        CircularBuffer<Integer> buffer = CircularBuffer.create(2);
        buffer.addAll(ImmutableList.of(1, 2));
        Iterator<Integer> iterator = buffer.iterator();
        iterator.next();
        iterator.remove();
        iterator.next();
        iterator.remove();
        assertTrue(buffer.isEmpty());
    }


    @Test
    public void testIteratorRemoveTwice() {
        CircularBuffer<Integer> buffer = CircularBuffer.create(2);
        buffer.addAll(ImmutableList.of(1, 2));
        Iterator<Integer> iterator = buffer.iterator();
        iterator.next();
        iterator.remove();
        Assertions.assertThrows(IllegalStateException.class,  iterator::remove);
    }

    @Test
    public void testIteratorRemoveWithoutNext() {
        CircularBuffer<Integer> buffer = CircularBuffer.create(2);
        buffer.addAll(ImmutableList.of(1, 2));
        Assertions.assertThrows(IllegalStateException.class, buffer.iterator()::remove);
    }

    @Test
    public void testIteratorAfterEnd() {
        CircularBuffer<Integer> buffer = CircularBuffer.create(1);
        buffer.add(1);
        Iterator<Integer> it = buffer.iterator();
        it.next();
        it.remove();
        Assertions.assertThrows(NoSuchElementException.class,  it::next);
    }
}
