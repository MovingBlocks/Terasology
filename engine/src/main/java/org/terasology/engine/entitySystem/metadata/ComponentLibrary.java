// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.metadata;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.Name;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.ModuleClassLibrary;
import org.terasology.reflection.reflect.ReflectFactory;

/**
 * The library for metadata about components (and their fields).
 */
public class ComponentLibrary extends ModuleClassLibrary<Component> {

    private static final Logger logger = LoggerFactory.getLogger(ComponentLibrary.class);

    public ComponentLibrary(ModuleEnvironment environment, ReflectFactory reflectFactory, CopyStrategyLibrary copyStrategyLibrary) {
        super(() -> environment, reflectFactory, copyStrategyLibrary);
    }

    private ComponentLibrary(ComponentLibrary componentLibrary, CopyStrategyLibrary newCopyStrategies) {
        super(componentLibrary, newCopyStrategies);
    }

    /**
     * @return a copy of the this library that uses the specified strategy for the specified type.
     */
    public <T> ComponentLibrary createCopyUsingCopyStrategy(Class<T> type, CopyStrategy<T> strategy) {
        CopyStrategyLibrary newCopyStrategies = copyStrategyLibrary.createCopyOfLibraryWithStrategy(type, strategy);
        return new ComponentLibrary(this, newCopyStrategies);
    }

    @Override
    protected <C extends Component> ClassMetadata<C, ?> createMetadata(Class<C> type, ReflectFactory factory,
                                                                       CopyStrategyLibrary copyStrategies, ResourceUrn uri) {
        ComponentMetadata<C> info;
        try {
            info = new ComponentMetadata<>(uri, type, factory, copyStrategies);
        } catch (NoSuchMethodException e) {
            logger.atError().log("Unable to register class {}: Default Constructor Required", type.getSimpleName(), e);
            return null;
        } catch (NoClassDefFoundError e) {
            // log what class was not found so that diagnosis is easier
            logger.atError().log("Class not found, {}", type.getSimpleName(), e);
            throw e;
        }
        return info;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Component> ComponentMetadata<T> getMetadata(Class<T> clazz) {
        return (ComponentMetadata<T>) super.getMetadata(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Component> ComponentMetadata<T> getMetadata(T object) {
        return (ComponentMetadata<T>) super.getMetadata(object);
    }

    public <T extends Component> T copyWithOwnedEntities(T object) {
        ComponentMetadata<T> info = getMetadata(object);
        if (info != null) {
            return info.copyWithOwnedEntities(object);
        }
        return null;
    }

    @Override
    public ComponentMetadata<? extends Component> getMetadata(ResourceUrn uri) {
        return (ComponentMetadata<? extends Component>) super.getMetadata(uri);
    }

    @Override
    public ComponentMetadata<? extends Component> resolve(String name) {
        return (ComponentMetadata<? extends Component>) super.resolve(name);
    }

    @Override
    public ComponentMetadata<?> resolve(String name, Name context) {
        return (ComponentMetadata<?>) super.resolve(name, context);
    }

    @Override
    public ComponentMetadata<?> resolve(String name, Module context) {
        return (ComponentMetadata<?>) super.resolve(name, context);
    }

    public Iterable<ComponentMetadata> iterateComponentMetadata() {
        return Iterables.filter(this, ComponentMetadata.class);
    }


    /**
     * Should not be called during the game, as the {@link org.terasology.engine.persistence.internal.ReadWriteStorageManager}
     * creates a copy of the data and uses the same instance in multiple threads.
     */
    @Override
    public void register(ResourceUrn uri, Class<? extends Component> clazz) {
        super.register(uri, clazz);
    }
}
