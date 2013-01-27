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
}
