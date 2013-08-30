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
import org.terasology.classMetadata.AbstractClassLibrary;
import org.terasology.classMetadata.ClassMetadata;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.reflect.ReflectFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.engine.SimpleUri;

/**
 * The library for metadata about components (and their fields).
 *
 * @author Immortius <immortius@gmail.com>
 */
public class ComponentLibrary extends AbstractClassLibrary<Component> {

    private static final Logger logger = LoggerFactory.getLogger(ComponentLibrary.class);
    private ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);

    public ComponentLibrary(ReflectFactory factory, CopyStrategyLibrary copyStrategies) {
        super(factory, copyStrategies);
    }

    @Override
    protected <C extends Component> ClassMetadata<C, ?> createMetadata(Class<C> type, ReflectFactory factory, CopyStrategyLibrary copyStrategies, SimpleUri uri) {
        ComponentMetadata<C> info;
        try {
            info = new ComponentMetadata<>(uri, type, factory, copyStrategies);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", type.getSimpleName(), e);
            return null;
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
    public ComponentMetadata<?> resolve(String name, String context) {
        return (ComponentMetadata<?>) super.resolve(name, context);
    }

    @Override
    public ComponentMetadata<?> resolve(String name, Module context) {
        return (ComponentMetadata<?>) super.resolve(name, context);
    }

    public Iterable<ComponentMetadata> iterateComponentMetadata() {
        return Iterables.filter(this, ComponentMetadata.class);
    }
}
