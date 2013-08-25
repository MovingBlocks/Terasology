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

package org.terasology.physics;

import javax.vecmath.Vector3f;

/**
 *
 * @author Xanhou
 */
public interface SweepCallback {

    /**
     * Given a SweepCallback, this method check for a safer slope to go with.
     * The main issue this is is used for is the voxel world. Each voxel is a
     * cube for the physics engine. If a player jumps between those voxels, you
     * get a weird slope, even though the player is moving on a flat area. This
     * method therefore also checks the sides.<bk>
     *
     * @param checkingOffset How far to go up and down from the current position
     * for the positions to check between.
     * @param originalSlope If no different slope can be found, this value is
     * returned, making this method always return a decent number.
     * @return a safer slope to use for character movement calculations.
     */
    float calculateSafeSlope(float originalSlope, float checkingOffset);
    
    /**
     * Returns where the closest hit took place.
     * @return 
     */
    Vector3f getHitNormalWorld();
    
    /**
     * Returns the normal of the surface that has been hit in the closest hit.
     * @return 
     */
    Vector3f getHitPointWorld();
    
    /**
     * How many times the requested checking distance needs to be travelled to
     * get to the location where the closes hit took place. When creating a
     * SweepCallback, you need to enter a distance to check for. If you take the
     * distance between the current location of your object and the location in
     * which it will be when it collides, than the distance between those points
     * is equal to this number times the distance given to check for.
     *
     * @return closestHitFraction
     */
    float getClosestHitFraction();
    
    /**
     * Note that if this is false, the other method make no sense and their
     * output may be not what you expect.
     *
     * @return true if there was at least one hit, false otherwise.
     */
    boolean hasHit();
    
    /**
     * Checks if the hit from this callback can be stepped upon.
     *
     * @param direction The direction to check in.
     * @param stepHeight The maximum step height.
     * @param slopeFactor If the slope you are walking against is smaller than
     * this, it is assumes the slope moving code activates and you no longer
     * need to take a step.
     * @param checkForwardDistance How far to check ahead for a place to step
     * upon.
     * @return true if it is possible to step up while moving into 'direction'.
     */
    public boolean checkForStep(Vector3f direction, float stepHeight, float slopeFactor, float checkForwardDistance);
}
