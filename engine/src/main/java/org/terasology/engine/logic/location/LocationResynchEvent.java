// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.location;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.engine.network.BroadcastEvent;
import org.terasology.gestalt.entitysystem.event.Event;

@BroadcastEvent
public class LocationResynchEvent implements Event {
    public Vector3f position = new Vector3f();
    public Quaternionf rotation = new Quaternionf();

    public LocationResynchEvent() {
    }

    public LocationResynchEvent(Vector3f position, Quaternionf rotation) {
        this.position.set(position);
        this.rotation.set(rotation);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Quaternionf getRotation() {
        return rotation;
    }
}
