// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.location;

import com.google.common.base.Preconditions;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.entitySystem.event.Event;

public class LocationChangedEvent implements Event {
    protected Vector3f lastPosition = new Vector3f();
    protected Quaternionf lastRotation = new Quaternionf();
    protected Vector3f currentPosition = new Vector3f();
    protected Quaternionf currentRotation = new Quaternionf();

    protected LocationChangedEvent() {
    }

    public LocationChangedEvent(Vector3fc lastPosition, Quaternionfc lastRotation, Vector3fc currentPosition, Quaternionfc currentRotation) {
        Preconditions.checkNotNull(lastPosition);
        Preconditions.checkNotNull(lastRotation);
        Preconditions.checkNotNull(currentPosition);
        Preconditions.checkNotNull(currentRotation);

        this.lastPosition.set(lastPosition);
        this.lastRotation.set(lastRotation);
        this.currentPosition.set(currentPosition);
        this.currentRotation.set(currentRotation);
    }

    public Vector3fc lastPosition() {
        return lastPosition;
    }

    public Quaternionfc lastRotation() {
        return lastRotation;
    }

    public Vector3fc currentPosition() {
        return currentPosition;
    }

    public Quaternionfc currentRotation() {
        return currentRotation;
    }
}
