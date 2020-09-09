// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.inventory.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;
import org.terasology.math.geom.Vector3f;

/**
 * Fire this event on an item in order for the authority to add the necessary components to put it in the world.
 */
@ServerEvent
public class DropItemEvent implements Event {
    private Vector3f position;

    public DropItemEvent() {
    }

    public DropItemEvent(Vector3f position) {
        this.position = position;
    }

    public Vector3f getPosition() {
        return position;
    }
}
