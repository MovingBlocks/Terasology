/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.entitySystem.sectors;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.gestalt.module.sandbox.API;

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
