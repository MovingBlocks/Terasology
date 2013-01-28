package org.terasology.utilities.collection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class CircularBufferTest {

    @Test
    public void addItems() {
        CircularBuffer<Integer> buffer = CircularBuffer.create(4);
        for (int i = 0; i < 100; ++i) {
            buffer.add(i);
            assertEquals((Integer)i, buffer.getLast());
        }
    }

    @Test
    public void removeItems() {
        CircularBuffer<Integer> buffer = CircularBuffer.create(4);
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);
        buffer.add(4);
        buffer.add(5);
        assertEquals(4, buffer.size());
        assertEquals((Integer)2, buffer.getFirst());
        assertEquals((Integer)2, buffer.popFirst());
        assertEquals((Integer)3, buffer.getFirst());
        assertEquals(3, buffer.size());
        assertEquals((Integer)5, buffer.getLast());
    }
}
