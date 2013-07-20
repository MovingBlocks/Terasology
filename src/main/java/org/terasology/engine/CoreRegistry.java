/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.engine;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Registry giving access to major singleton systems, via the interface they fulfil.
 *
 * @author Immortius <immortius@gmail.com>
 */
public class CoreRegistry {
    private static Map<Class<? extends Object>, Object> store = Maps.newConcurrentMap();
    private static Set<Class<? extends Object>> permStore = Sets.newSetFromMap(Maps.<Class<? extends Object>, Boolean>newConcurrentMap());

    /**
     * Registers a core system
     *
     * @param type   The interface which the system fulfils
     * @param object The system itself
     * @param <T>
     */
    public static <T, U extends T> U put(Class<T> type, U object) {
        store.put(type, object);
        return object;
    }

    /**
     * Registers a core system
     *
     * @param type   The interface which the system fulfils
     * @param object The system itself
     * @param <T>
     */
    public static <T, U extends T> U putPermanently(Class<T> type, U object) {
        store.put(type, object);
        permStore.add(type);
        return object;
    }

    /**
     * @param type
     * @param <T>
     * @return The system fulfilling the given interface
     */
    public static <T> T get(Class<T> type) {
        return type.cast(store.get(type));
    }

    public static void clear() {
        Iterator<Map.Entry<Class<?>, Object>> iterator = store.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Class<?>, Object> entry = iterator.next();
            if (!permStore.contains(entry.getKey())) {
                iterator.remove();
            }
        }
    }

    public static <T> void remove(Class<T> type) {
        store.remove(type);
    }

    private CoreRegistry() {
    }
}
