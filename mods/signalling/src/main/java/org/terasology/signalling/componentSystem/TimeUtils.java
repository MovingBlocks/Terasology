package org.terasology.signalling.componentSystem;

import org.terasology.world.time.WorldTime;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class TimeUtils {
    public static long getCorrectTime(WorldTime time) {
        return (long) (time.getMilliseconds()/time.getTimeRate());
    }
}
