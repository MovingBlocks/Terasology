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
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.metadata.copying.CopyStrategyLibrary;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.EventMetadata;
import org.terasology.entitySystem.metadata.reflect.ReflectFactory;

/**
 * @author Immortius
 */
public class EventLibraryImpl extends BaseLibraryImpl<Event, EventMetadata<? extends Event>> implements EventLibrary {
    private static final Logger logger = LoggerFactory.getLogger(EventLibraryImpl.class);
    private CopyStrategyLibrary copyStrategies;
    private ReflectFactory reflectFactory;

    public EventLibraryImpl(ReflectFactory reflectFactory, CopyStrategyLibrary copyStrategies) {
        super();
        this.copyStrategies = copyStrategies;
        this.reflectFactory = reflectFactory;
    }

    @Override
    public String getNameFor(Class<? extends Event> clazz) {
        return clazz.getSimpleName();
    }

    @Override
    protected <CLASS extends Event> EventMetadata<? extends Event> createMetadata(Class<CLASS> clazz, String primaryName) {
        EventMetadata<CLASS> info;
        try {
            info = new EventMetadataImpl<>(clazz, copyStrategies, reflectFactory, primaryName);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", clazz.getSimpleName(), e);
            return null;
        }
        return info;
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
    @SuppressWarnings("unchecked")
    public EventMetadata<? extends Event> getMetadata(String className) {
        return (EventMetadata<? extends Event>) super.getMetadata(className);
    }

}
