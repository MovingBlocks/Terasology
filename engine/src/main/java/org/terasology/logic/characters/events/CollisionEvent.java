// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.events;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.event.Event;

/**
 */
public class CollisionEvent implements Event {
    private Vector3f velocity;
    private Vector3f location;

    public CollisionEvent(Vector3f location, Vector3f velocity) {
        this.location = new Vector3f(location);
        this.velocity = new Vector3f(velocity);
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Vector3f getLocation() {
        return location;
    }
}
