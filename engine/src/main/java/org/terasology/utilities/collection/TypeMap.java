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

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * TypeMap is a map-like class specialised for holding a set of objects by their class. So it is a {@code Map<Class<? extends T>, T>} where the value is guaranteed to be
 * of the type of the key.
 *
 */
public final class TypeMap<T> {

    private Map<Class<? extends T>, T> inner;

    private TypeMap(Map<Class<? extends T>, T> inner) {
        this.inner = inner;
    }

    public static <T> TypeMap<T> create() {
        return new TypeMap<>(Maps.<Class<? extends T>, T>newHashMap());
    }

    public static <T> TypeMap<T> create(Map<Class<? extends T>, T> withMap) {
        return new TypeMap<>(withMap);
    }

    public Map<Class<? extends T>, T> asMap() {
        return Collections.unmodifiableMap(inner);
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

    public <U extends T> U get(Class<U> key) {
        T value = inner.get(key);
        if (value != null) {
            return key.cast(value);
        }
        return null;
    }

    public <U extends T> U put(Class<U> key, U value) {
        return key.cast(inner.put(key, value));
    }

    public <U extends T> U remove(Class<U> key) {
        return key.cast(inner.remove(key));
    }

    public void clear() {
        inner.clear();
    }

    public Set<Class<? extends T>> keySet() {
        return inner.keySet();
    }

    public Collection<T> values() {
        return inner.values();
    }

    public Set<Map.Entry<Class<? extends T>, T>> entrySet() {
        return inner.entrySet();
    }
}
