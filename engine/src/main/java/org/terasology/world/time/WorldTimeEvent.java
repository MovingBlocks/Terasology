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

import org.terasology.entitySystem.event.Event;

import com.google.common.math.LongMath;

/**
 * A timer event that represents a (world-based) time instant
 * @author Immortius
 * @author Martin Steiger
 */
public class WorldTimeEvent implements Event {
    private long worldTimeMS;
    private long timeInDay;

    public WorldTimeEvent(long worldTimeMS) {
        this.worldTimeMS = worldTimeMS;
        this.timeInDay = LongMath.mod(worldTimeMS, WorldTime.DAY_LENGTH);
    }

    /**
     * @return the world time in milli secs
     */
    public long getWorldTimeMS() {
        return worldTimeMS;
    }

    /**
     * @return the world time in days
     */
    public float getWorldTime() {
        return WorldTime.MS_TO_DAYS * worldTimeMS;
    }

    /**
     * @return true if at dawn/sunrise (true exactly once per day)
     */
    public boolean isDawn() {
        return timeInDay == WorldTime.DAWN_TIME;
    }

    /**
     * @return true if at midday/noon (true exactly once per day)
     */
    public boolean isMidday() {
        return timeInDay == WorldTime.MIDDAY_TIME;
    }

    /**
     * @return true if at dusk/sunset (true exactly once per day)
     */
    public boolean isDusk() {
        return timeInDay == WorldTime.DUSK_TIME;
    }

    /**
     * @return true if after DAWN and before MIDDAY
     */
    public boolean isMorning() {
        return timeInDay > WorldTime.DAWN_TIME && timeInDay < WorldTime.MIDDAY_TIME;
    }

    /**
     * @return true if after MIDDAY and before DUSK
     */
    public boolean isAfternoon() {
        return timeInDay > WorldTime.MIDDAY_TIME && timeInDay < WorldTime.DUSK_TIME;
    }

    /**
     * @return true if after DUSK and before DAWN
     */
    public boolean isNight() {
        return timeInDay > WorldTime.DUSK_TIME || timeInDay < WorldTime.DAWN_TIME;
    }

    /**
     * @return true if at midnight (true exactly once per day)
     */
    public boolean isMidnight() {
        return timeInDay == WorldTime.MIDNIGHT_TIME;
    }

    @Override
    public String toString() {
        return String.format("WorldTimeEvent [%s ms -> %.2f days (%s)]", worldTimeMS, getWorldTime(), getDayTimeText());
    }

    private String getDayTimeText() {
        if (isDusk()) {
            return "dusk";
        }

        if (isMorning()) {
            return "morning";
        }

        if (isMidday()) {
            return "midday";
        }

        if (isAfternoon()) {
            return "afternoon";
        }

        if (isDawn()) {
            return "dawn";
        }

        if (isNight()) {
            return "night";
        }

        if (isMidnight()) {
            return "mignight";
        }

        return "UNDEFINED";
    }
}
