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
import org.terasology.entitySystem.Component;

/**
 * The library for metadata about components (and their fields).
 *
 * @author Immortius <immortius@gmail.com>
 */
public class ComponentLibrary extends AbstractClassLibrary<Component> {

    private static final Logger logger = LoggerFactory.getLogger(ComponentLibrary.class);

    public ComponentLibrary(ReflectFactory factory, CopyStrategyLibrary copyStrategies) {
        super(factory, copyStrategies);
    }

    @Override
    protected String getNameFor(Class<? extends Component> type) {
        return MetadataUtil.getComponentClassName(type);
    }

    @Override
    protected <CLASS extends Component> ClassMetadata<CLASS, ?> createMetadata(Class<CLASS> type, ReflectFactory factory, CopyStrategyLibrary copyStrategies, String name) {
        ComponentMetadata<CLASS> info;
        try {
            info = new ComponentMetadata<>(type, factory, copyStrategies, name);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", type.getSimpleName(), e);
            return null;
        }
        return info;
    }

    @Override
    public <T extends Component> ComponentMetadata<T> getMetadata(Class<T> clazz) {
        return (ComponentMetadata<T>) super.getMetadata(clazz);
    }

    @Override
    public <T extends Component> ComponentMetadata<T> getMetadata(T object) {
        return (ComponentMetadata<T>) super.getMetadata(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ComponentMetadata<? extends Component> getMetadata(String className) {
        return (ComponentMetadata<? extends Component>) super.getMetadata(className);
    }

    public Iterable<ComponentMetadata> iterateComponentMetadata() {
        return Iterables.filter(this, ComponentMetadata.class);
    }
}
