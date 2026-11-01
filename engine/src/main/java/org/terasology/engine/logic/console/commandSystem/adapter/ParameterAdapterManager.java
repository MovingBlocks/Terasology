// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem.adapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.context.annotation.API;
import org.terasology.gestalt.naming.Name;
import org.terasology.engine.world.block.family.BlockFamily;

import java.util.Map;

@API
public class ParameterAdapterManager {
    private final Map<Class<?>, ParameterAdapter> adapters = Maps.newHashMap();

    /**
     * @return A manager with basic adapters for wrapped primitives and {@link String}
     */
    @SuppressWarnings("unchecked")
    public static ParameterAdapterManager createBasic() {
        ParameterAdapterManager manager = new ParameterAdapterManager();

        for (Map.Entry<Class, ParameterAdapter> entry : PrimitiveAdapters.MAP.entrySet()) {
            manager.registerAdapter(entry.getKey(), entry.getValue());
        }

        return manager;
    }

    /**
     * @return A manager with basic adapters and following classes:
     * {@link org.terasology.engine.entitySystem.prefab.Prefab}
     */
    public static ParameterAdapterManager createCore() {
        ParameterAdapterManager manager = createBasic();

        manager.registerAdapter(Name.class, new NameAdapter());
        manager.registerAdapter(Prefab.class, new PrefabAdapter());
        manager.registerAdapter(BlockFamily.class, new BlockFamilyAdapter());

        return manager;
    }

    /**
     * @return {@code true}, if the adapter didn't override a previously present adapter
     */
    public <T> boolean registerAdapter(Class<? extends T> clazz, ParameterAdapter<T> adapter) {
        return adapters.put(clazz, adapter) == null;
    }

    public boolean isAdapterRegistered(Class<?> clazz) {
        return adapters.containsKey(clazz);
    }

    /**
     * @param clazz The type of the returned object
     * @param raw The string from which to parse
     * @return The parsed object
     * @throws ClassCastException If the {@link ParameterAdapter} is linked with an incorrect {@link java.lang.Class}.
     */
    @SuppressWarnings("unchecked")
    public <T> T parse(Class<T> clazz, String raw) throws ClassCastException {
        Preconditions.checkNotNull(raw, "The String to parse must not be null");

        ParameterAdapter adapter = getAdapter(clazz);

        Preconditions.checkNotNull(adapter, "No adapter found for " + clazz.getCanonicalName());

        return (T) adapter.parse(raw);
    }

    /**
     * @param value The object to convertToString
     * @param clazz The class pointing to the desired adapter
     * @return The composed object
     * @throws ClassCastException If the {@link ParameterAdapter} is linked with an incorrect {@link java.lang.Class}.
     */
    @SuppressWarnings("unchecked")
    public <T> String convertToString(T value, Class<? super T> clazz) throws ClassCastException {
        Preconditions.checkNotNull(value, "The Object to convertToString must not be null");

        ParameterAdapter adapter = getAdapter(clazz);

        Preconditions.checkNotNull(adapter, "No adapter found for " + clazz.getCanonicalName());

        return adapter.convertToString(value);
    }

    /**
     * @param value The object to convertToString
     * @return The composed object
     * @throws ClassCastException If the {@link ParameterAdapter} is linked with an incorrect {@link java.lang.Class}.
     */
    @SuppressWarnings("unchecked")
    public String convertToString(Object value) throws ClassCastException {
        Class<?> clazz = value.getClass();

        return convertToString(value, (Class<? super Object>) clazz);
    }

    public ParameterAdapter getAdapter(Class<?> clazz) {
        return adapters.get(clazz);
    }
}
