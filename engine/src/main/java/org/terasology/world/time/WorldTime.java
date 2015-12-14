/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.time;

import org.terasology.entitySystem.systems.ComponentSystem;

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
