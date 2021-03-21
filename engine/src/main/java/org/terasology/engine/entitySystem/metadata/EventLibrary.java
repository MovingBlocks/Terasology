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
package org.terasology.engine.entitySystem.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.ModuleClassLibrary;
import org.terasology.reflection.reflect.ReflectFactory;

/**
 * The library for metadata about events (and their fields).
 *
 */
public class EventLibrary extends ModuleClassLibrary<Event> {

    private static final Logger logger = LoggerFactory.getLogger(EventLibrary.class);

    public EventLibrary(ModuleEnvironment environment, ReflectFactory reflectFactory, CopyStrategyLibrary copyStrategyLibrary) {
        super(environment, reflectFactory, copyStrategyLibrary);
    }

    @Override
    protected <C extends Event> ClassMetadata<C, ?> createMetadata(Class<C> type, ReflectFactory factory, CopyStrategyLibrary copyStrategies, ResourceUrn uri) {
        try {
            return new EventMetadata<>(type, copyStrategies, factory, uri);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", type.getSimpleName(), e);
            return null;
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> EventMetadata<T> getMetadata(Class<T> clazz) {
        return (EventMetadata<T>) super.getMetadata(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> EventMetadata<T> getMetadata(T object) {
        return (EventMetadata<T>) super.getMetadata(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public EventMetadata<? extends Event> getMetadata(ResourceUrn uri) {
        return (EventMetadata<? extends Event>) super.getMetadata(uri);
    }

}
