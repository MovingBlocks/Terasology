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
package org.terasology.entitySystem.metadata.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.copying.CopyStrategyLibrary;
import org.terasology.entitySystem.metadata.reflect.ReflectFactory;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class ComponentLibraryImpl extends BaseLibraryImpl<Component, ComponentMetadata<? extends Component>> implements ComponentLibrary {
    private static final Logger logger = LoggerFactory.getLogger(ComponentLibraryImpl.class);

    private CopyStrategyLibrary copyStrategies;
    private ReflectFactory reflectFactory;

    public ComponentLibraryImpl(ReflectFactory factory, CopyStrategyLibrary copyStrategies) {
        super();
        this.reflectFactory = factory;
        this.copyStrategies = copyStrategies;
    }

    @Override
    public String getNameFor(Class<? extends Component> clazz) {
        return MetadataUtil.getComponentClassName(clazz);
    }

    @Override
    protected <CLASS extends Component> ComponentMetadata<? extends Component> createMetadata(Class<CLASS> clazz, String name) {
        ComponentMetadata<CLASS> info;
        try {
            info = new ComponentMetadataImpl<>(clazz, copyStrategies, reflectFactory, name);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", clazz.getSimpleName(), e);
            return null;
        }
        return info;
    }

    @Override
    public <T extends Component> ComponentMetadataImpl<T> getMetadata(Class<T> clazz) {
        return (ComponentMetadataImpl<T>) super.getMetadata(clazz);
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

}
