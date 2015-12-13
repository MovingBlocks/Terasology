/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.physics.engine;

import org.terasology.math.geom.Vector3f;

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
