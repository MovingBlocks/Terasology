/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.entitySystem.metadata;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.Component;
import org.terasology.module.Module;
import org.terasology.naming.Name;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.AbstractClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.reflect.ReflectFactory;

/**
 * The library for metadata about components (and their fields).
 */
public class ComponentLibrary extends AbstractClassLibrary<Component> {

    private static final Logger logger = LoggerFactory.getLogger(ComponentLibrary.class);

    public ComponentLibrary(Context context) {
        super(context);
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
    protected <C extends Component> ClassMetadata<C, ?> createMetadata(Class<C> type, ReflectFactory factory, CopyStrategyLibrary copyStrategies, SimpleUri uri) {
        ComponentMetadata<C> info;
        try {
            info = new ComponentMetadata<>(uri, type, factory, copyStrategies);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", type.getSimpleName(), e);
            return null;
        } catch (NoClassDefFoundError e) {
            // log what class was not found so that diagnosis is easier
            logger.error("Class not found, {}", type.getSimpleName(), e);
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

    @Override
    public ComponentMetadata<? extends Component> getMetadata(SimpleUri uri) {
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
     * Should not be called during the game, as the {@link org.terasology.persistence.internal.ReadWriteStorageManager}
     * creates a copy of the data and uses the same instance in multiple threads.
     */
    @Override
    public void register(SimpleUri uri, Class<? extends Component> clazz) {
        super.register(uri, clazz);
    }
}
