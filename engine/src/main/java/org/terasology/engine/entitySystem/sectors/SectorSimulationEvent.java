// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.sectors;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.context.annotation.API;

/**
 * This is the event sent to all sector-level entities by the {@link SectorSimulationSystem}, allowing them to do simulation. It
 * is sent regardless of whether the chunk the entity is in is loaded or not.
 *
 * @see SectorSimulationEvent#getDelta()
 */
@API
public class SectorSimulationEvent implements Event {
    private long delta;

    /**
     * @see SectorSimulationEvent#getDelta() for the delta parameter
     */
    protected SectorSimulationEvent(long delta) {
        this.delta = delta;
    }

    /**
     * This gives the time elapsed, in ms, since the last time this event was sent to the given {@link EntityRef}.
     *
     * Performing simulation based on the the number of times this event is sent is not reliable, because there may be
     * big variations in the time between sending these events (notably, an event will be sent whenever the chunk an
     * entity is in is loaded, even if one has just been sent.
     *
     * Using the delta will give a reliable measure of how much simulation to perform.
     *
     * @return the time, in ms, since the last time this event was sent to the given entity
     */
    public long getDelta() {
        return delta;
    }
}
