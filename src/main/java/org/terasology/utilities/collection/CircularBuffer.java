/*
 * Copyright 2013 Moving Blocks
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

import java.util.Iterator;

/**
 * @author Immortius
 */
public class CircularBuffer<T> implements Iterable<T> {
    private T[] buffer;
    private int startIndex = 0;
    private int occupancy;

    public static <T> CircularBuffer<T> create(int length) {
        return new CircularBuffer<T>(length);
    }

    private CircularBuffer(int length) {
        buffer = (T[]) new Object[length];
    }

    public T get(int index) {
        if (index >= occupancy) {
            throw new IndexOutOfBoundsException();
        }
        return buffer[calculateIndex(index)];
    }

    public T getLast() {
        return get(occupancy - 1);
    }

    public void add(T item) {
        buffer[(startIndex + occupancy) % buffer.length] = item;
        if (occupancy < buffer.length) {
            occupancy++;
        } else {
            startIndex = (startIndex + 1) % buffer.length;
        }
    }

    public T getFirst() {
        return buffer[startIndex];
    }

    public T popFirst() {
        T result = buffer[startIndex];
        buffer[startIndex] = null;
        startIndex++;
        occupancy--;
        return result;
    }

    public boolean isEmpty() {
        return size() == 0;
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


    @Override
    public Iterator<T> iterator() {
        return new BufferIterator();
    }

    public void clear() {
        occupancy = 0;
    }

    private class BufferIterator implements Iterator<T> {
        int index = 0;

        @Override
        public boolean hasNext() {
            return index < occupancy;
        }

        @Override
        public T next() {
            return get(index++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
