// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.sun;

/**
 * An interface for celestial systems (with only one sun)
 */
public interface CelestialSystem {

    /**
     * @return angle of the sun in radians
     */
    float getSunPosAngle();

    /**
     * Toggles the halting of sun's position and angle
     *
     * @param timeInDays
     */
    void toggleSunHalting(float timeInDays);

    /**
     * @return Whether the sun is currently halted or not
     */
    boolean isSunHalted();
}
