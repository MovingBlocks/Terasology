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
import org.terasology.entitySystem.metadata.MetadataUtil;
import org.terasology.entitySystem.metadata.reflected.ReflectedComponentMetadata;
import org.terasology.entitySystem.metadata.TypeHandlerLibrary;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class ComponentLibraryImpl extends BaseLibraryImpl<Component, ComponentMetadata<? extends Component>> implements ComponentLibrary {
    private static final Logger logger = LoggerFactory.getLogger(ComponentLibraryImpl.class);

    private TypeHandlerLibrary metadataBuilder;

    public ComponentLibraryImpl(TypeHandlerLibrary metadataBuilder) {
        super();
        this.metadataBuilder = metadataBuilder;
    }

    @Override
    public String[] getNamesFor(Class<? extends Component> clazz) {
        return new String[]{
                MetadataUtil.getComponentClassName(clazz),
                clazz.getSimpleName()
        };
    }

    @Override
    public <T extends Component> ReflectedComponentMetadata<T> getMetadata(Class<T> clazz) {
        return (ReflectedComponentMetadata<T>) super.getMetadata(clazz);
    }

    @Override
    public <T extends Component> ComponentMetadata<T> getMetadata(T object) {
        return (ComponentMetadata<T>) super.getMetadata(object);
    }

    @Override
    public ComponentMetadata<? extends Component> getMetadata(String className) {
        return (ComponentMetadata<? extends Component>) super.getMetadata(className);
    }

    @Override
    protected <U extends Component> ComponentMetadata<U> createMetadata(Class<U> clazz, String... names) {
        ComponentMetadata<U> info;
        try {
            info = new ReflectedComponentMetadata<>(clazz, names);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", clazz.getSimpleName(), e);
            return null;
        }

        metadataBuilder.populateFields(clazz, info, false);
        return info;
    }
}
