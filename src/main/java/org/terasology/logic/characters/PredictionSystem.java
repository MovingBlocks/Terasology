package org.terasology.logic.characters;

import org.terasology.entitySystem.EntityRef;

/**
 * Interface for the system that provides the ability to compensate for lag, by rewinding and replaying state
 *
 * @author Immortius
 */
public interface PredictionSystem {
    /**
     * Rewinds time for the specified client
     * @param client The client entity to rewind for
     * @param timeMs The time to rewind to
     */
    public void lagCompensate(EntityRef client, long timeMs);

    public void restoreToPresent();
}
