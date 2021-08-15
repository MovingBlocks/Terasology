// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.sun;

/**
 * Describes the movement of celestial bodies.
 */
public interface CelestialModel {

    /**
     * @return angle of the sun in radians
     */
    float getSunPosAngle(float days);

    /**
     * @param day the day
     * @return offset in milli-secs.
     */
    long getDawn(long day);

    /**
     * @param day the day
     * @return offset in milli-secs.
     */
    long getMidday(long day);

    /**
     * @param day the day
     * @return offset in milli-secs.
     */
    long getDusk(long day);

    /**
     * @param day the day
     * @return offset in milli-secs.
     */
    long getMidnight(long day);
}
