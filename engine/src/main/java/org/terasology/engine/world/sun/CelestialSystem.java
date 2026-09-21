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

    /**
     * @return the current phase of the moon, as a value in [0, 1) where 0 (and the limit at 1) is new
     * moon and 0.5 is full moon. Intended as a hook for anything wanting to key off the moon's phase -
     * rendering, gameplay, a future astronomical/calendar system - without needing its own day-counting
     * logic. See #94.
     */
    float getMoonPhase();
}
