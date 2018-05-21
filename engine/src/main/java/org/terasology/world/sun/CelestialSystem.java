/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.sun;

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
