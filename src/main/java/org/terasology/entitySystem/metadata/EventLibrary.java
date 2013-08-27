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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.classMetadata.AbstractClassLibrary;
import org.terasology.classMetadata.ClassMetadata;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.reflect.ReflectFactory;
import org.terasology.entitySystem.event.Event;

/**
 * The library for metadata about events (and their fields).
 *
 * @author Immortius <immortius@gmail.com>
 */
public class EventLibrary extends AbstractClassLibrary<Event> {

    private static final Logger logger = LoggerFactory.getLogger(EventLibrary.class);

    public EventLibrary(ReflectFactory reflectFactory, CopyStrategyLibrary copyStrategies) {
        super(reflectFactory, copyStrategies);
    }

    @Override
    protected String getNameFor(Class<? extends Event> type) {
        return type.getSimpleName();
    }

    @Override
    protected <CLASS extends Event> ClassMetadata<CLASS, ?> createMetadata(Class<CLASS> type, ReflectFactory factory, CopyStrategyLibrary copyStrategies, String name) {
        try {
            return new EventMetadata<>(type, copyStrategies, factory, name);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", type.getSimpleName(), e);
            return null;
        }
    }


    @Override
    public <T extends Event> EventMetadata<T> getMetadata(Class<T> clazz) {
        return (EventMetadata<T>) super.getMetadata(clazz);
    }

    @Override
    public <T extends Event> EventMetadata<T> getMetadata(T object) {
        return (EventMetadata<T>) super.getMetadata(object);
    }

    @Override
    public EventMetadata<? extends Event> getMetadata(String className) {
        return (EventMetadata<? extends Event>) super.getMetadata(className);
    }

}
