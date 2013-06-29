package org.terasology.world.time;

import org.terasology.entitySystem.systems.ComponentSystem;

/**
 * @author Immortius
 */
public interface WorldTime extends ComponentSystem {

    static final int DAY_LENGTH = 1000 * 60 * 60 * 24;

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
     * @return World time in days
     */
    float getDays();

    /**
     * The world time progresses at a different rate to game time, generally faster.
     * @return The ratio of world time to real time.
     */
    float getTimeRate();

    /**
     * Immediately updates the world time
     * @param time
     */
    void setMilliseconds(long time);

    /**
     * Sets the world time in terms of days
     * @param timeInDays
     */
    void setDays(float timeInDays);
}
