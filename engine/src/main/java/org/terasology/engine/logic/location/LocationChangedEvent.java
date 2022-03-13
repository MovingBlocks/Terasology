// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.location;

import com.google.common.base.Preconditions;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * A <i>notification event</i> that the location and/or rotation of the associated entity has changed.
 * <p>
 * This is similar to a {@link org.terasology.engine.entitySystem.event.BeforeAfterEvent BeforeAfterEvent} but holds the
 * last and current values for both {@code position} and {@code rotation}.
 */
public class LocationChangedEvent implements Event {
    protected Vector3f lastPosition = new Vector3f();
    protected Quaternionf lastRotation = new Quaternionf();
    protected Vector3f currentPosition = new Vector3f();
    protected Quaternionf currentRotation = new Quaternionf();

    /**
     * INTERNAL: default constructor for de-/serialization.
     */
    protected LocationChangedEvent() {
    }

    /**
     * Create a new <i>notification event</i> about a location and/or rotation change.
     *
     * @param lastPosition the previous position; must not be {@code null}
     * @param lastRotation the previous rotation; must not be {@code null}
     * @param currentPosition the current position; must not be {@code null}
     * @param currentRotation the current rotation; must not be {@code null}
     */
    public LocationChangedEvent(Vector3fc lastPosition, Quaternionfc lastRotation,
                                Vector3fc currentPosition, Quaternionfc currentRotation) {
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

    /**
     * Compute the vector that has to be added to get from {@link #lastPosition()} to {@link #currentPosition()} and
     * store the result in {@code dest}.
     * <p>
     * If V is the computed difference between {@code last} and {@code current}, then the following equation holds:
     * <p>
     * <code>last + V = current</code>
     *
     * @param dest will hold the result
     * @return {@code dest}
     */
    public Vector3f positionDelta(Vector3f dest) {
        return currentPosition.sub(lastPosition, dest);
    }

    /**
     * Compute the rotation that has to be applied to get from {@link #lastRotation()} to {@link #currentRotation()} and
     * store the result in {@code dest}.
     * <p>
     * If D is the computed difference between {@code last} and {@code current}, then the following equation holds:
     * <p>
     * <code>last * D = current</code>
     *
     * @param dest will hold the result
     * @return {@code dest}
     */
    public Quaternionf rotationDelta(Quaternionf dest) {
        return lastRotation.difference(currentRotation, dest);
    }
}
