// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.console.commandSystem.adapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.module.sandbox.API;
import org.terasology.naming.Name;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;
import org.terasology.world.block.family.BlockFamily;

import java.util.Map;

@API
public class ParameterAdapterManager {
    private final Map<Class<?>, ParameterAdapter> adapters = Maps.newHashMap();

    @In
    private ContextAwareClassFactory classFactory;

    /**
     * Command adapter registration.
     */
    public void registerBaseAdapters() {
        Preconditions.checkState(adapters.isEmpty(), "ParameterAdapterManager already registered");

        for (Map.Entry<Class, ParameterAdapter> entry : PrimitiveAdapters.MAP.entrySet()) {
            registerAdapter(entry.getKey(), entry.getValue());
        }

        registerAdapter(Name.class, NameAdapter.class);
        registerAdapter(Prefab.class, PrefabAdapter.class);
        registerAdapter(BlockFamily.class, BlockFamilyAdapter.class);
    }

    /**
     * @return {@code true}, if the adapter didn't override a previously present adapter
     * @deprecated Use {@link ParameterAdapterManager#registerAdapter(Class, Class)} istead. adds DI feature for
     *         ParameterAdapter.
     */
    public <T> boolean registerAdapter(Class<? extends T> clazz, ParameterAdapter<T> adapter) {
        return adapters.put(clazz, adapter) == null;
    }

    /**
     * @return {@code true}, if the adapter didn't override a previously present adapter
     */
    public <T> boolean registerAdapter(Class<T> clazz, Class<? extends ParameterAdapter<T>> implementationClazz) {
        return adapters.put(clazz, classFactory.createWithContext(implementationClazz)) == null;
    }

    public boolean isAdapterRegistered(Class<?> clazz) {
        return adapters.containsKey(clazz);
    }

    /**
     * @param clazz The type of the returned object
     * @param raw The string from which to parse
     * @return The parsed object
     * @throws ClassCastException If the {@link ParameterAdapter} is linked with an incorrect {@link
     *         java.lang.Class}.
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
     * @throws ClassCastException If the {@link ParameterAdapter} is linked with an incorrect {@link
     *         java.lang.Class}.
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
     * @throws ClassCastException If the {@link ParameterAdapter} is linked with an incorrect {@link
     *         java.lang.Class}.
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
