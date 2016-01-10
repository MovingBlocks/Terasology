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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 */
public class CharSequenceIterator implements Iterator<Character> {

    private final CharSequence sequence;
    private int pos;

    public CharSequenceIterator(CharSequence sequence) {
        // Please note that, in order to reduce overhead, this class doesn't copy the sequence,
        // so changes in mutable sequences (like StringBuilders or StringBuffers) will be reflected here
        this.sequence = sequence;
    }

    @Override
    public boolean hasNext() {
        return pos < sequence.length();
    }

    // The method nextChar is preferred over this one, as it does not create a wrapper for the primitive
    @Override
    public Character next() {
        return nextChar();
    }

    public char nextChar() {
        // A try-catch has a virtually zero overhead for in-bounds indexes, so I am using it instead of checking the position
        try {
            return sequence.charAt(pos++);
        } catch (IndexOutOfBoundsException exception) {
            throw new NoSuchElementException("Reached end of CharSequence");
        }
    }

    @Override
    public void remove() {
        // Actually some character sequences are mutable, but for simplicity I am just omitting this method
        throw new UnsupportedOperationException("CharSequence objects are immutable");
    }
}
