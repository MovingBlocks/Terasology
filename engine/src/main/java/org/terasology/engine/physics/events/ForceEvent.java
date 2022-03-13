// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.events;

import org.joml.Vector3f;
import org.terasology.engine.network.BroadcastEvent;
import org.terasology.gestalt.entitysystem.event.Event;

@BroadcastEvent
public class ForceEvent implements Event {
    private Vector3f force;

    protected ForceEvent() {
    }

    public ForceEvent(Vector3f force) {
        this.force = force;
    }

    public Vector3f getForce() {
        return force;
    }
}
