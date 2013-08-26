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

import org.terasology.entitySystem.event.Event;

/**
 * The library for metadata about events (and their fields).
 *
 * @author Immortius <immortius@gmail.com>
 */
public interface EventLibrary extends ClassLibrary<Event, EventMetadata<? extends Event>> {

    /**
     * @param clazz
     * @return The metadata for the given event class, or null if not registered.
     */
    <T extends Event> EventMetadata<T> getMetadata(Class<T> clazz);

    /**
     * @param event
     * @return The metadata for the given event, or null if not registered.
     */
    <T extends Event> EventMetadata<T> getMetadata(T event);

    /**
     * @param eventName
     * @return The metadata for the given event name, or null if not registered.
     */
    EventMetadata<? extends Event> getMetadata(String eventName);

}
