package org.terasology.entitySystem.common;

import java.util.Iterator;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class NullIterator<T> implements Iterator<T>, Iterable<T> {
    
    private static NullIterator instance = new NullIterator();

    public static <T> NullIterator<T> newInstance() {
        return instance;
    }

    private NullIterator() {}

    public boolean hasNext() {
        return false;
    }

    public T next() {
        return null;
    }

    public void remove() {
    }

    public Iterator<T> iterator() {
        return this;
    }
}
