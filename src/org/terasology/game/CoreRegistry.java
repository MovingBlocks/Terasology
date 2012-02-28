package org.terasology.game;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Registry giving access to major singleton systems, via the interface they fulfil.
 * @author Immortius <immortius@gmail.com>
 */
public class CoreRegistry {
    private static Map<Class<? extends Object>, Object> store = Maps.newHashMap();

    /**
     * Registers a core system
     * @param type The interface which the system fulfils
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

    private CoreRegistry() {}
}
