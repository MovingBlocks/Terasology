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
 * A simple implementations of {@link CelestialSystem} with constant daily events
 * and perfect radial movement of a single sun.
 *
 * @author Martin Steiger
 */
public class BasicCelestialModel implements CelestialModel {
    public static final long DAY_LENGTH = 1000 * 60 * 60 * 24 / 50;


    @Override
    public float getSunPosAngle(long gameTime) {
        return (float) (getDay(gameTime) * 2.0 * Math.PI - Math.PI);  // offset by 180 deg.;
    }

    @Override
    public float getDawn(long day) {
        return 0.25f;
    }

    @Override
    public float getMidday(long day) {
        return 0.5f;
    }

    @Override
    public float getDusk(long day) {
        return 0.75f;
    }

    @Override
    public float getMidnight(long day) {
        return 0f;
    }

    @Override
    public float getDay(long gameTime) {
        // Offsetting by DAWN_TIME, so that the game starts at dawn
        return 1f * (gameTime + 0.25f) / DAY_LENGTH;
    }
}
