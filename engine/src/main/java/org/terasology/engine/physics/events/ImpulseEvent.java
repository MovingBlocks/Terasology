// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.events;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;

@BroadcastEvent
public class ImpulseEvent implements Event {
    private Vector3f impulse;

    protected ImpulseEvent() {
    }

    public ImpulseEvent(Vector3f impulse) {
        this.impulse = impulse;
    }

    public Vector3f getImpulse() {
        return impulse;
    }
}
