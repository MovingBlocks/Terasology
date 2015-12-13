/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.time;

import java.math.RoundingMode;

import com.google.common.base.Preconditions;
import com.google.common.math.DoubleMath;

/**
 * A timer event that represents a (world-based) time instant
 */
public class WorldTimeEvent extends TimeEventBase {

    public WorldTimeEvent(long worldTimeMS) {
        super(worldTimeMS);
    }

    public boolean matchesDaily(float fraction) {
        Preconditions.checkArgument(fraction >= 0 && fraction <= 1, "fraction must be in [0..1]");

        long fracInMs = DoubleMath.roundToLong(fraction * WorldTime.DAY_LENGTH, RoundingMode.HALF_UP);
        long diff = getDayTimeInMs() - fracInMs;

        return 2 * diff < WorldTime.TICK_EVENT_RATE && 2 * diff >= -WorldTime.TICK_EVENT_RATE;
    }

    @Override
    public String toString() {
        return String.format("WorldTimeEvent [%s ms -> %.2f days]", getWorldTimeInMs(), getWorldTime());
    }
}
