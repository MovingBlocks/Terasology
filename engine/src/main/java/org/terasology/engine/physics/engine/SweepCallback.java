// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.engine;


import org.joml.Vector3f;

/**
 * Provides the results from a CharacterCollider sweep()
 *
 */
public interface SweepCallback {

    /**
     * This method determines the average slope of the hit location. This can differ from that provided by the hit normal, as the hit normal
     * is subject to local features and discontinuities. Using an average protects against this.
     *
     * @param originalSlope  If no different slope can be found, this value is returned, making this method always return a decent number.
     * @param checkingOffset How far to go up and down from the current position for the positions to check between.
     * @return a safer slope to use for character movement calculations.
     */
    float calculateAverageSlope(float originalSlope, float checkingOffset);

    /**
     * Returns the normal of the surface that has been hit in the closest hit.
     *
     * @return
     */
    Vector3f getHitNormalWorld();

    /**
     * Returns where the closest hit took place.
     *
     * @return
     */
    Vector3f getHitPointWorld();

    /**
     * The fraction of ray distance travelled before the first contact.
     *
     * @return closestHitFraction
     */
    float getClosestHitFraction();

    /**
     * Note that if this is false, the other method make no sense and their output may be not what you expect.
     *
     * @return true if there was at least one hit, false otherwise.
     */
    boolean hasHit();

    /**
     * Checks if the hit from this callback can be stepped upon.
     *
     * @param direction            The direction to check in.
     * @param stepHeight           The maximum step height.
     * @param slopeFactor          If the slope you are walking against is smaller than this, it is assumes the slope moving code activates and you
     *                             no longer need to take a step.
     * @param checkForwardDistance How far to check ahead for a place to step upon.
     * @return true if it is possible to step up while moving into 'direction'.
     */
    boolean checkForStep(Vector3f direction, float stepHeight, float slopeFactor, float checkForwardDistance);
}
