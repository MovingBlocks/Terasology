// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.characters;

import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * Interface for the system that provides the ability to compensate for lag, by rewinding and replaying state
 *
 */
public interface PredictionSystem {
    /**
     * Rewinds time for the specified client
     *
     * @param client The client entity to rewind for
     * @param timeMs The time to rewind to
     */
    void lagCompensate(EntityRef client, long timeMs);

    void restoreToPresent();
}
