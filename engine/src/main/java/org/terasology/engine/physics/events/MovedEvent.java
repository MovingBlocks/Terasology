// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.events;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.gestalt.entitysystem.event.Event;

public class MovedEvent implements Event {
    private Vector3f delta = new Vector3f();
    private Vector3f finalPosition = new Vector3f();

    public MovedEvent(Vector3fc delta, Vector3fc finalPosition) {
        this.delta.set(delta);
        this.finalPosition.set(finalPosition);
    }

    public Vector3fc getDelta() {
        return delta;
    }

    public Vector3fc getPosition() {
        return finalPosition;
    }
}
