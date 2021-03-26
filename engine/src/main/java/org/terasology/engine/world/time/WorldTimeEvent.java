// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.time;

import com.google.common.base.Preconditions;
import com.google.common.math.DoubleMath;

import java.math.RoundingMode;

/**
 * A timer event that represents a (world-based) time instant
 */
public class WorldTimeEvent extends TimeEventBase {

    public WorldTimeEvent(long worldTimeMS) {
        super(worldTimeMS);
    }

    public boolean matchesDaily(float fraction) {
        Preconditions.checkArgument(fraction >= 0 && fraction <= 1, "fraction must be in [0..1]");

        long fracInMs = DoubleMath.roundToLong((double) fraction * WorldTime.DAY_LENGTH, RoundingMode.HALF_UP);
        long diff = getDayTimeInMs() - fracInMs;

        return 2 * diff < WorldTime.TICK_EVENT_RATE && 2 * diff >= -WorldTime.TICK_EVENT_RATE;
    }

    @Override
    public String toString() {
        return String.format("WorldTimeEvent [%s ms -> %.2f days]", getWorldTimeInMs(), getWorldTime());
    }
}
