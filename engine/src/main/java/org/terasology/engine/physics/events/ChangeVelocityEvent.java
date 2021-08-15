// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.events;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;

@BroadcastEvent
public class ChangeVelocityEvent implements Event {
    private Vector3f linearVelocity;
    private Vector3f angularVelocity;

    // Default constructor for serialization
    ChangeVelocityEvent() {
    }

    public ChangeVelocityEvent(Vector3f linearVelocity) {
        this(linearVelocity, null);
    }

    public ChangeVelocityEvent(Vector3f linearVelocity, Vector3f angularVelocity) {
        this.linearVelocity = linearVelocity;
        this.angularVelocity = angularVelocity;
    }

    public Vector3f getLinearVelocity() {
        return linearVelocity;
    }

    public Vector3f getAngularVelocity() {
        return angularVelocity;
    }
}
