// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.math.geom.Vector3f;

/**
 *
 */
public class CollisionEvent implements Event {
    private final Vector3f velocity;
    private final Vector3f location;

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
