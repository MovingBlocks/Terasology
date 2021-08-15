// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.sun;

import org.terasology.engine.world.time.WorldTime;

/**
 * A simple implementations of {@link CelestialSystem} with constant daily events
 * and perfect radial movement of a single sun.
 */
public class BasicCelestialModel implements CelestialModel {

    private static final long DAWN_TIME = WorldTime.DAY_LENGTH / 4;
    private static final long MIDDAY_TIME = WorldTime.DAY_LENGTH / 2;
    private static final long DUSK_TIME = 3 * WorldTime.DAY_LENGTH / 4;
    private static final long MIDNIGHT_TIME = 0;

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
