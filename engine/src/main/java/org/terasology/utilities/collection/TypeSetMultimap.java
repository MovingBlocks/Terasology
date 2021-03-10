// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
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

    @Override
    public <U extends T> Set<U> get(Class<U> key) {
        return convertSet(key, inner.get(key));
    }

    @Override
    public <U extends T> Set<U> removeAll(Class<U> key) {
        return convertSet(key, inner.removeAll(key));
    }

    @Override
    public <U extends T> Set<U> replaceValues(Class<U> key, Iterable<? extends U> values) {
        return convertSet(key, inner.replaceValues(key, values));
    }

    @Override
    public Set<Map.Entry<Class<? extends T>, T>> entries() {
        return inner.entries();
    }

    private <U extends T> Set<U> convertSet(Class<U> type, Set<T> values) {
        return values.stream().map(type::cast).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
