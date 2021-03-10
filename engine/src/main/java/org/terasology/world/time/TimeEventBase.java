// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.time;

import com.google.common.math.LongMath;
import org.terasology.engine.entitySystem.event.Event;

/**
 * A base class for different timer events
 */
public abstract class TimeEventBase implements Event {

    private long worldTimeMS;
    private long timeInDay;

    public TimeEventBase(long worldTimeMS) {
        this.worldTimeMS = worldTimeMS;
        this.timeInDay = LongMath.mod(worldTimeMS, WorldTime.DAY_LENGTH);
    }

    /**
     * @return the time of day as a fraction
     */
    public float getDayTime() {
        return timeInDay / (float) WorldTime.DAY_LENGTH;
    }

    /**
     * @return the time of day in milli secs
     */
    public long getDayTimeInMs() {
        return timeInDay;
    }

    /**
     * @return the world time in days
     */
    public float getWorldTime() {
        return worldTimeMS / (float) WorldTime.DAY_LENGTH;
    }

    /**
     * @return the world time in milli secs
     */
    public long getWorldTimeInMs() {
        return worldTimeMS;
    }
}
