// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.collection;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * TypeMap is a multimap-like class specialised for holding a set of objects by their class. So it is a {@code Multimap<Class<? extends T>, T>} where the value is guaranteed
 * to be of the type of the key.
 *
 */
public abstract class TypeMultimap<T> {

    private Multimap<Class<? extends T>, T> inner;

    TypeMultimap(Multimap<Class<? extends T>, T> inner) {
        this.inner = inner;
    }

    public Multimap<Class<? extends T>, T> asMultimap() {
        return inner;
    }

    public int size() {
        return inner.size();
    }

    public boolean isEmpty() {
        return inner.isEmpty();
    }

    public boolean containsKey(Class<? extends T> key) {
        return inner.containsKey(key);
    }

    public boolean containsValue(T value) {
        return inner.containsValue(value);
    }

    public <U extends T> boolean containsEntry(Class<U> key, U value) {
        return inner.containsEntry(key, value);
    }

    public <U extends T> boolean put(Class<U> key, U value) {
        return inner.put(key, value);
    }

    public <U extends T> boolean remove(Class<U> key, U value) {
        return inner.remove(key, value);
    }

    public <U extends T> boolean putAll(Class<U> key, Iterable<? extends U> values) {
        return inner.putAll(key, values);
    }

    public void clear() {
        inner.clear();
    }

    public Set<Class<? extends T>> keySet() {
        return inner.keySet();
    }

    public Multiset<Class<? extends T>> keys() {
        return inner.keys();
    }

    public Collection<T> values() {
        return inner.values();
    }

    public Map<Class<? extends T>, Collection<T>> asMap() {
        return inner.asMap();
    }

    public abstract <U extends T> Collection<U> get(Class<U> key);

    public abstract <U extends T> Collection<U> removeAll(Class<U> key);

    public abstract <U extends T> Collection<U> replaceValues(Class<U> key, Iterable<? extends U> values);

    public abstract Collection<Map.Entry<Class<? extends T>, T>> entries();

}
