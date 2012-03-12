package org.terasology.game;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class CoreRegistry {
    private static Map<Class<? extends Object>, Object> store = Maps.newHashMap();

    public static <T> void put(Class<T> type, T object) {
        store.put(type, object);
    }
    
    public static <T> T get(Class<T> type) {
        return type.cast(store.get(type));
    }

    private CoreRegistry() {}
}
