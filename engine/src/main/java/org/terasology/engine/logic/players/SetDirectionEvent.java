// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.OwnerEvent;

/**
 * Trigger event that sets the direction a player is facing.
 */
@OwnerEvent
public class SetDirectionEvent implements Event {
    private float yaw;
    private float pitch;

    protected SetDirectionEvent() {
    }

    public SetDirectionEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
