/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.entitySystem;

/**
 * Event system propagates events to registered handlers
 *
 * @author Immortius <immortius@gmail.com>
 */
public interface EventSystem {

    /**
     * Process all pending events
     */
    public void process();

    /**
     * Registers an event
     *
     * @param id
     * @param eventType
     */
    void registerEvent(String id, Class<? extends Event> eventType);

    /**
     * Registers an object as an event handler - all methods with the {@link ReceiveEvent} annotation will be registered
     *
     * @param handler
     */
    void registerEventHandler(EventHandlerSystem handler);

    /**
     * Registers an event receiver object
     *
     * @param eventReceiver
     * @param eventClass
     * @param componentTypes
     * @param <T>
     */
    <T extends Event> void registerEventReceiver(EventReceiver<T> eventReceiver, Class<T> eventClass, Class<? extends Component>... componentTypes);

    /**
     * @param eventReceiver
     * @param eventClass
     * @param priority
     * @param componentTypes
     * @param <T>
     */
    <T extends Event> void registerEventReceiver(EventReceiver<T> eventReceiver, Class<T> eventClass, int priority, Class<? extends Component>... componentTypes);

    /**
     * Sends an event to all handlers for an entity's components
     *
     * @param entity
     * @param event
     */
    void send(EntityRef entity, Event event);

    /**
     * Sends an event to a handlers for a specific component of an entity
     *
     * @param entity
     * @param event
     * @param component
     */
    void send(EntityRef entity, Event event, Component component);
}
