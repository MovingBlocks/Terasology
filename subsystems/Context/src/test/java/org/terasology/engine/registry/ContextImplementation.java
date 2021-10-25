// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.registry;

import com.google.common.collect.Maps;
import org.terasology.engine.context.Context;

import java.util.Map;

public class ContextImplementation implements Context {
    private final Map<Class<?>, Object> map = Maps.newConcurrentMap();

    @Override
    public <T> T get(Class<? extends T> type) {
        T result = type.cast(map.get(type));
        if (result != null) {
            return result;
        }
        return null;
    }

    @Override
    public <T, U extends T> void put(Class<T> type, U object) {
        map.put(type, object);
    }
}
