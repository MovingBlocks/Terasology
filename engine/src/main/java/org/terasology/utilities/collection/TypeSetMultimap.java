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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class TypeSetMultimap<T> extends TypeMultimap<T> {

    private SetMultimap<Class<? extends T>, T> inner;

    TypeSetMultimap(SetMultimap<Class<? extends T>, T> inner) {
        super(inner);
        this.inner = inner;
    }

    public TypeSetMultimap<T> create() {
        return new TypeSetMultimap<>(HashMultimap.<Class<? extends T>, T>create());
    }

    public TypeSetMultimap<T> createFrom(SetMultimap<Class<? extends T>, T> from) {
        return new TypeSetMultimap<>(from);
    }

    public <U extends T> Set<U> get(Class<U> key) {
        return convertSet(key, inner.get(key));
    }

    public <U extends T> Set<U> removeAll(Class<U> key) {
        return convertSet(key, inner.removeAll(key));
    }

    public <U extends T> Set<U> replaceValues(Class<U> key, Iterable<? extends U> values) {
        return convertSet(key, inner.replaceValues(key, values));
    }

    public Set<Map.Entry<Class<? extends T>, T>> entries() {
        return inner.entries();
    }

    private <U extends T> Set<U> convertSet(Class<U> type, Set<T> values) {
        Set<U> results = Sets.newLinkedHashSetWithExpectedSize(values.size());
        for (T value : values) {
            results.add(type.cast(value));
        }
        return results;
    }
}
