// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.inventory.events;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Fire this event on an item in order for the authority to add the necessary components to put it in the world.
 */
@ServerEvent
public class DropItemEvent implements Event {
    private Vector3f position = new Vector3f();

    public DropItemEvent() {
    }

    public DropItemEvent(Vector3fc position) {
        this.position.set(position);
    }

    public Vector3fc getPosition() {
        return position;
    }
}
