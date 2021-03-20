// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.collection;

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
