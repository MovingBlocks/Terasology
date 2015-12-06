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

import static org.terasology.world.time.WorldTime.DAY_LENGTH;

/**
 * A simple implementations of {@link CelestialSystem} with constant daily events
 * and perfect radial movement of a single sun.
 */
public class BasicCelestialModel implements CelestialModel {

    private static final long DAWN_TIME = DAY_LENGTH / 4;
    private static final long MIDDAY_TIME = DAY_LENGTH / 2;
    private static final long DUSK_TIME = 3 * DAY_LENGTH / 4;
    private static final long MIDNIGHT_TIME = DAY_LENGTH;

    @Override
    public float getSunPosAngle(float days) {
        return (float) (days * 2.0 * Math.PI - Math.PI);  // offset by 180 deg.;
    }

    @Override
    public long getDawn(long day) {
        return DAWN_TIME;
    }

    @Override
    public long getMidday(long day) {
        return MIDDAY_TIME;
    }

    @Override
    public long getDusk(long day) {
        return DUSK_TIME;
    }

    @Override
    public long getMidnight(long day) {
        return MIDNIGHT_TIME;
    }
}
