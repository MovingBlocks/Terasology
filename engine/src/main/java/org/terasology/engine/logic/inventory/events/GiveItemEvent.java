// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.inventory.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Gives the entity to the target entity
 */
@ServerEvent
public class GiveItemEvent implements Event {
    EntityRef targetEntity = EntityRef.NULL;
    boolean handled;

    public GiveItemEvent() {
    }

    public GiveItemEvent(EntityRef targetEntity) {
        // ensure that null values do not happen, replace with correct null reference
        this.targetEntity = targetEntity == null ? EntityRef.NULL : targetEntity;
    }

    public EntityRef getTargetEntity() {
        return targetEntity;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}
