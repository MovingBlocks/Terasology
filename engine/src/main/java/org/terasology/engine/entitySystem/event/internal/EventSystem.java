// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.event.internal;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.systems.ComponentSystem;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Event system propagates events to registered handlers
 *
 */
public interface EventSystem {

    /**
     * Process all pending events
     */
    void process();

    /**
     * Registers an event
     *
     * @param uri
     * @param eventType
     */
    void registerEvent(ResourceUrn uri, Class<? extends Event> eventType);

    /**
     * Registers an object as an event handler - all methods with the {@link org.terasology.engine.entitySystem.event.ReceiveEvent} annotation will be registered
     *
     * @param handler
     */
    void registerEventHandler(ComponentSystem handler);

    /**
     * Unregister an object as an event handler.
     * @param handler
     */
    void unregisterEventHandler(ComponentSystem handler);

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

    <T extends Event> void unregisterEventReceiver(EventReceiver<T> eventReceiver, Class<T> eventClass, Class<? extends Component>... componentTypes);

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

    /**
     * Change main thread to current thread.
     */
    void setToCurrentThread();
}
