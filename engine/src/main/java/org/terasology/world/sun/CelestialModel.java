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
 * Describes the movement of celestial bodies.
 * @author Martin Steiger
 */
public interface CelestialModel {
    /**
     * @return angle of the sun in radians
     */
    float getSunPosAngle(long gameTime);

    /**
     * @param day the day
     * @return offset in milli-secs.
     */
    float getDawn(long day);

    /**
     * @param day the day
     * @return offset in milli-secs.
     */
    float getMidday(long day);

    /**
     * @param day the day
     * @return offset in milli-secs.
     */
    float getDusk(long day);

    /**
     * @param day the day
     * @return offset in milli-secs.
     */
    float getMidnight(long day);

    float getDay(long gameTime);
}
