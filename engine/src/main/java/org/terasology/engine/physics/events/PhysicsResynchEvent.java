// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.events;

import org.joml.Vector3f;
import org.terasology.engine.network.BroadcastEvent;
import org.terasology.gestalt.entitysystem.event.Event;

@BroadcastEvent
public class PhysicsResynchEvent implements Event {
    private Vector3f velocity = new Vector3f();
    private Vector3f angularVelocity = new Vector3f();

    protected PhysicsResynchEvent() {
    }

    public PhysicsResynchEvent(Vector3f velocity, Vector3f angularVelocity) {
        this.velocity.set(velocity);
        this.angularVelocity.set(angularVelocity);
    }

    /**
     * @return the linear velocity of the physics entity when this event is sent. Copy to use.
     */
    public Vector3f getVelocity() {
        return velocity;
    }

    /**
     * @return The angular or rotational velocity of the physics entity when this event is sent. Copy to use.
     */
    public Vector3f getAngularVelocity() {
        return angularVelocity;
    }
}
