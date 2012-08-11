/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.game;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Registry giving access to major singleton systems, via the interface they fulfil.
 *
 * @author Immortius <immortius@gmail.com>
 */
public class CoreRegistry {
    private static Map<Class<? extends Object>, Object> store = Maps.newHashMap();

    /**
     * Registers a core system
     *
     * @param type   The interface which the system fulfils
     * @param object The system itself
     * @param <T>
     */
    public static <T> void put(Class<T> type, T object) {
        store.put(type, object);
    }

    /**
     * @param type
     * @param <T>
     * @return The system fulfilling the given interface
     */
    public static <T> T get(Class<T> type) {
        return type.cast(store.get(type));
    }

    private CoreRegistry() {
    }
}
