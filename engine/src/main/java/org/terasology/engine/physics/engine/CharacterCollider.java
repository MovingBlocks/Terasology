// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.engine;

import org.joml.Vector3f;

/**
 * A character collider is a non-blocking collider for use calculating character movement.
 * A character collider is always a vertically-aligned capsule.
 *
 */
public interface CharacterCollider {

    boolean isPending();

    /**
     * Retrieves the location of this collider.
     *
     * @return out the result.
     */
    Vector3f getLocation();

    /**
     * Sets the worlds location of this collider.
     *
     * @param loc
     */
    void setLocation(Vector3f loc);

    /**
     * A sweep is like a ray trace, except it moves the character collider from startPos to endPos, detecting any intersections as it moves.
     *
     * @param startPos
     * @param endPos
     * @param allowedPenetration
     * @param slopeFactor
     * @return A sweep callback with the results of the sweep
     */
    SweepCallback sweep(Vector3f startPos, Vector3f endPos, float allowedPenetration, float slopeFactor);
}
