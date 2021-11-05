// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

public class PendingEvent {
    private EntityRef entity;
    private Event event;
    private Component component;

     public PendingEvent(EntityRef entity, Event event) {
        this.event = event;
        this.entity = entity;
    }

     public PendingEvent(EntityRef entity, Event event, Component component) {
        this.entity = entity;
        this.event = event;
        this.component = component;
    }

    public EntityRef getEntity() {
        return entity;
    }

    public Event getEvent() {
        return event;
    }

    public Component getComponent() {
        return component;
    }
}
