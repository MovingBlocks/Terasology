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

package org.terasology.utilities.collection;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A circular/cyclic/ring buffer. Adding elements is allowed only at the end of the buffer. 
 * Removing elements can be done through {@link #popFirst()} and {@link #popLast()} or {@link #remove(int)}.
 */
public final class CircularBuffer<T> extends AbstractList<T> {
    private final T[] buffer;
    private int startIndex;
    private int occupancy;

    private CircularBuffer(int length) {
        buffer = (T[]) new Object[length];
    }

    public static <T> CircularBuffer<T> create(int length) {
        return new CircularBuffer<>(length);
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= occupancy) {
            throw new IndexOutOfBoundsException();
        }
        return buffer[calculateIndex(index)];
    }

    @Override
    public T set(int index, T element) {
        if (index < 0 || index >= occupancy) {
            throw new IndexOutOfBoundsException();
        }
        
        int bufIndex = calculateIndex(index);
        T prev = buffer[bufIndex];
        buffer[bufIndex] = element;
        return prev;
    }

    /**
     * @return the last element in the buffer
     */
    public T getLast() {
        return get(occupancy - 1);
    }

    @Override
    public boolean add(T item) {
        buffer[(startIndex + occupancy) % buffer.length] = item;
        if (occupancy < buffer.length) {
            occupancy++;
        } else {
            startIndex = (startIndex + 1) % buffer.length;
        }
        return true;
    }

    @Override
    public T remove(int idx) {
        if (idx < 0 || idx >= occupancy) {
            throw new IndexOutOfBoundsException();
        }
        
        T old = buffer[calculateIndex(idx)];

        // shift all elements that are on the right side of element by one
        for (int i = idx; i < occupancy - 1; i++) {
            int thisIdx = calculateIndex(i);
            int nextIdx = calculateIndex(i + 1);

            buffer[thisIdx] = buffer[nextIdx];
        }

        buffer[calculateIndex(occupancy - 1)] = null;
        occupancy--;
        
        return old;
    }
    
    /**
     * @return the first element in the buffer
     */
    public T getFirst() {
        return buffer[startIndex];
    }

    /**
     * Removes the first element from the buffer
     * @return the first element
     */
    public T popFirst() {
        T result = buffer[startIndex];
        buffer[startIndex] = null;
        startIndex++;
        occupancy--;
        return result;
    }

    /**
     * Removes the last element from the buffer
     * @return the last element
     */
    public T popLast() {
        int index = calculateIndex(occupancy - 1);
        T result = buffer[index];
        buffer[index] = null;
        occupancy--;
        return result;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        return occupancy;
    }

    /**
     * @return the maximum size of the buffer
     */
    public int capacity() {
        return buffer.length;
    }


    private int calculateIndex(int relativeIndex) {
        return (relativeIndex + startIndex) % buffer.length;
    }

    @Override
    public Iterator<T> iterator() {
        return new BufferIterator();
    }

    @Override
    public void clear() {
        occupancy = 0;
        startIndex = 0;
    }

    private class BufferIterator implements Iterator<T> {
        private int index;
        private int prevIndex = -1;

        @Override
        public boolean hasNext() {
            return index < occupancy;
        }

        @Override
        public T next() {
            if (index >= occupancy) {
                throw new NoSuchElementException();
            }
            
            prevIndex = index;
            return get(index++);
        }

        @Override
        public void remove() {
            if (prevIndex >= 0) {
                CircularBuffer.this.remove(prevIndex);
                index = prevIndex;
                prevIndex = -1;
            } else {
                throw new IllegalStateException();
            }
        }
    }
}
