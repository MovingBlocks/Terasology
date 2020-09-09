// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.location;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.math.geom.BaseVector3f;
import org.terasology.math.geom.ImmutableQuat4f;
import org.terasology.math.geom.ImmutableVector3f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

public class LocationChangedEvent implements Event {
    public final LocationComponent component;
    public final ImmutableVector3f oldPosition;
    public final ImmutableQuat4f oldRotation;
    public final ImmutableVector3f newPosition;
    public final ImmutableQuat4f newRotation;

    public LocationChangedEvent(LocationComponent newLocation) {
        this(newLocation, newLocation.position, newLocation.rotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition) {
        this(newLocation, oPosition, newLocation.rotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Vector3f nPosition) {
        this(newLocation, oPosition, newLocation.rotation, nPosition, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Quat4f oRotation) {
        this(newLocation, newLocation.position, oRotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Quat4f oRotation, Quat4f nRotation) {
        this(newLocation, newLocation.position, oRotation, newLocation.position, nRotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Quat4f oRotation) {
        this(newLocation, oPosition, oRotation, newLocation.position, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Quat4f oRotation,
                                Vector3f nPosition) {
        this(newLocation, oPosition, oRotation, nPosition, newLocation.rotation);
    }

    public LocationChangedEvent(LocationComponent newLocation, Vector3f oPosition, Quat4f oRotation, Quat4f nRotation) {
        this(newLocation, oPosition, oRotation, newLocation.position, nRotation);
    }

    public LocationChangedEvent(LocationComponent nComponent, Vector3f oPosition, Quat4f oRotation,
                                Vector3f nPosition, Quat4f nRotation) {
        oldPosition = ImmutableVector3f.createOrUse(oPosition);
        oldRotation = new ImmutableQuat4f(oRotation.x, oRotation.y, oRotation.z, oRotation.w);
        newPosition = ImmutableVector3f.createOrUse(nPosition);
        newRotation = new ImmutableQuat4f(nRotation.x, nRotation.y, nRotation.z, nRotation.w);
        component = nComponent;
    }

    public BaseVector3f vectorMoved() {
        return oldPosition != null ? newPosition.sub(oldPosition) : Vector3f.zero();
    }

    public float distanceMoved() {
        return oldPosition != null ? newPosition.distance(oldPosition) : 0.0F;
    }
}
