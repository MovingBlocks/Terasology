package org.terasology.world.time;

import org.terasology.entitySystem.systems.ComponentSystem;

/**
 * @author Immortius
 */
public interface WorldTime extends ComponentSystem {

    /**
     * @return World time in milliseconds.
     */
    long getTimeInMs();

    /**
     * @return World time in seconds
     */
    float getTime();

    /**
     * World time starts at midnight of the first day being 0, midnight of the second day being 1 and so on.
     * @return World time in days
     */
    float getTimeInDays();

    /**
     * The world time progresses at a different rate to game time, generally faster.
     * @return The ratio of world time to real time.
     */
    float getTimeRate();

    /**
     * Immediately updates the world time
     * @param time
     */
    void setTime(long time);

    /**
     * Sets the world time in terms of days
     * @param timeInDays
     */
    void setTimeInDays(float timeInDays);
}
