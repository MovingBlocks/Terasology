// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.time;

import org.terasology.engine.entitySystem.systems.ComponentSystem;

/**
 */
public interface WorldTime extends ComponentSystem {

    /**
     * The length of a day in milli-seconds
     */
    long DAY_LENGTH = 1000 * 60 * 60 * 24;

    /**
     * The number of timer tick events per day.
     * This must be a divisor of {@link #DAY_LENGTH} to avoid rounding issues.
     */
    long TICK_EVENTS_PER_DAY = 100;

    long TICK_EVENT_RATE = DAY_LENGTH / TICK_EVENTS_PER_DAY;

    /**
     * The offset used by the game to start at 12 noon.
     */
    float NOON_OFFSET = 0.50f;

    /**
     * The offset used by the game to start at sunrise
     */
    float SUNRISE_OFFSET = 0.3f;

    /**
     * The offset used by the game to start at sunset
     */
    float SUNSET_OFFSET = 0.7f;


    /**
     * @return World time in milliseconds.
     */
    long getMilliseconds();

    /**
     * @return World time in seconds
     */
    float getSeconds();

    /**
     * World time starts at midnight of the first day being 0, midnight of the second day being 1 and so on.
     *
     * @return World time in days
     */
    float getDays();

    /**
     * The world time progresses at a different rate to game time, generally faster.
     *
     * @return The ratio of world time to real time.
     */
    float getTimeRate();

    /**
     * Immediately updates the world time
     *
     * @param time
     */
    void setMilliseconds(long time);

    /**
     * Sets the world time in terms of days
     *
     * @param timeInDays
     */
    void setDays(float timeInDays);
}
