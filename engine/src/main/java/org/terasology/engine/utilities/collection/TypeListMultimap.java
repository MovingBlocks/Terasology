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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 */
public class TypeListMultimap<T> extends TypeMultimap<T> {

    private ListMultimap<Class<? extends T>, T> inner;

    TypeListMultimap(ListMultimap<Class<? extends T>, T> inner) {
        super(inner);
        this.inner = inner;
    }

    public TypeListMultimap<T> create() {
        return new TypeListMultimap<>(ArrayListMultimap.<Class<? extends T>, T>create());
    }

    public TypeListMultimap<T> createFrom(ListMultimap<Class<? extends T>, T> from) {
        return new TypeListMultimap<>(from);
    }

    @Override
    public <U extends T> List<U> get(Class<U> key) {
        return convertList(key, inner.get(key));
    }

    @Override
    public <U extends T> List<U> removeAll(Class<U> key) {
        return convertList(key, inner.removeAll(key));
    }

    @Override
    public <U extends T> List<U> replaceValues(Class<U> key, Iterable<? extends U> values) {
        return convertList(key, inner.replaceValues(key, values));
    }

    @Override
    public Collection<Map.Entry<Class<? extends T>, T>> entries() {
        return inner.entries();
    }

    private <U extends T> List<U> convertList(Class<U> type, Collection<T> values) {
        return values.stream().map(type::cast).collect(Collectors.toCollection(ArrayList::new));
    }

}
