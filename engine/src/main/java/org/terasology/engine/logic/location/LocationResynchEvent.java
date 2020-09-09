// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.location;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

@BroadcastEvent
public class LocationResynchEvent implements Event {
    public Vector3f position;
    public Quat4f rotation;

    public LocationResynchEvent() {
    }

    public LocationResynchEvent(Vector3f position, Quat4f rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Quat4f getRotation() {
        return rotation;
    }
}
