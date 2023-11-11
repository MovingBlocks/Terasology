// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.bootstrap.eventSystem;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.internal.EventReceiver;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.entitySystem.systems.ComponentSystem;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.Event;

public abstract class AbstractEventSystemDecorator implements EventSystem {
    private final EventSystem eventSystem;
    private Thread mainThread;

    public AbstractEventSystemDecorator(EventSystem eventSystem) {
        this.mainThread = Thread.currentThread();
        this.eventSystem = eventSystem;
    }

    public boolean currentThreadIsMain() {
        return mainThread.equals(Thread.currentThread());
    }

    @Override
    public void process() {
        eventSystem.process();
    }

    @Override
    public void registerEvent(ResourceUrn uri, Class<? extends Event> eventType) {
        eventSystem.registerEvent(uri, eventType);
    }

    @Override
    public void registerEventHandler(ComponentSystem handler) {
        eventSystem.registerEventHandler(handler);
    }

    @Override
    public void unregisterEventHandler(ComponentSystem handler) {
        eventSystem.unregisterEventHandler(handler);
    }

    @Override
    public <T extends Event> void registerEventReceiver(EventReceiver<T> eventReceiver, Class<T> eventClass, Class<?
            extends Component>... componentTypes) {
        eventSystem.registerEventReceiver(eventReceiver, eventClass, componentTypes);
    }

    @Override
    public <T extends Event> void registerEventReceiver(EventReceiver<T> eventReceiver, Class<T> eventClass,
                                                        int priority, Class<? extends Component>... componentTypes) {
        eventSystem.registerEventReceiver(eventReceiver, eventClass, priority, componentTypes);
    }

    @Override
    public <T extends Event> void unregisterEventReceiver(EventReceiver<T> eventReceiver, Class<T> eventClass, Class<
            ? extends Component>... componentTypes) {
        eventSystem.unregisterEventReceiver(eventReceiver, eventClass, componentTypes);
    }

    @Override
    public void send(EntityRef entity, Event event) {
        eventSystem.send(entity, event);
    }

    @Override
    public void send(EntityRef entity, Event event, Component component) {
        eventSystem.send(entity, event, component);
    }

    @Override
    public void setToCurrentThread() {
        eventSystem.setToCurrentThread();
        mainThread = Thread.currentThread();
    }
}
