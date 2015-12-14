/*
 * Copyright 2014 MovingBlocks
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
