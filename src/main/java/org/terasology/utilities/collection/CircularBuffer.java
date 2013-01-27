package org.terasology.utilities.collection;

import java.util.Deque;

/**
 * @author Immortius
 */
public class CircularBuffer<T> {
    private Object[] buffer;
    private int startIndex = 0;
    private int occupancy;

    public static <T> CircularBuffer<T> create(int length) {
        return new CircularBuffer<T>(length);
    }

    private CircularBuffer(int length) {
        buffer = new Object[length];
    }

    public T get(int index) {
        if (index >= occupancy) {
            throw new IndexOutOfBoundsException();
        }
        return (T) buffer[calculateIndex(index)];
    }

    public T getLast() {
        return get(occupancy - 1);
    }

    public void add(T item) {
        buffer[(startIndex + occupancy) % buffer.length] = item;
        if (occupancy < buffer.length) {
            occupancy++;
        } else {
            startIndex++;
        }
    }

    public int size() {
        return occupancy;
    }

    public int capacity() {
        return buffer.length;
    }


    private int calculateIndex(int index) {
        index += startIndex;
        return index % buffer.length;
    }


}
