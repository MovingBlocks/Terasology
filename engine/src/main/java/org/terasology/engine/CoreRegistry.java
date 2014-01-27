/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine;

/**
 * @author Immortius
 * @deprecated Please use org.terasology.registry.CoreRegistry instead. Support for this class will be dropped in the future.
 */
@API
@Deprecated
public final class CoreRegistry {

    private CoreRegistry() {
    }

    /**
     * Registers a core system
     *
     * @param type   The interface which the system fulfils
     * @param object The system itself
     * @param <T>
     */
    public static <T, U extends T> U put(Class<T> type, U object) {
        return org.terasology.registry.CoreRegistry.put(type, object);
    }

    /**
     * Registers a core system
     *
     * @param type   The interface which the system fulfils
     * @param object The system itself
     * @param <T>
     */
    public static <T, U extends T> U putPermanently(Class<T> type, U object) {
        return org.terasology.registry.CoreRegistry.putPermanently(type, object);
    }

    /**
     * @param type
     * @param <T>
     * @return The system fulfilling the given interface
     */
    public static <T> T get(Class<T> type) {
        return org.terasology.registry.CoreRegistry.get(type);
    }

    public static void clear() {
        org.terasology.registry.CoreRegistry.clear();
    }

    public static <T> void remove(Class<T> type) {
        org.terasology.registry.CoreRegistry.remove(type);
    }

}

