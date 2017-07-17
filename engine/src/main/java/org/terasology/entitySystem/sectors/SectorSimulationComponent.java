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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.internal.BaseEntityRef;
import org.terasology.module.sandbox.API;
import org.terasology.protobuf.EntityData;

/**
 * The component that allows the {@link SectorSimulationSystem} to send simulation events to a sector-scope entity.
 *
 * This should be automatically added by either {@link EntityManager#createSectorEntity(long)} or
 * {@link BaseEntityRef#setScope(EntityData.Entity.Scope)}, so modules should not need to modify or add it.
 */
@API
public class SectorSimulationComponent implements Component {

    public static final float MAX_DELTA_DEFAULT = 10;

    /**
     * The maximum time that can elapse between {@link SectorSimulationEvent}s being sent. This value does not change
     * the fact that a simulation event is always sent when the chunk the entity is in is loaded.
     *
     * TODO: this should only affect the timing of events sent when the chunk is not loaded; a different value should
     * TODO: be used when the chunk is loaded. This will allow this value to be set as high as possible (or even to a
     * TODO: non-simulating value) if all of the simulation can be postponed until chunk load.
     *
     * This should be set as high as possible, so that fewer simulation events need to be sent in total. The purpose of
     * this value is to allow checking for whether its borders need to be expanded regularly, so that the appropriate
     * events are called if those expanded regions are loaded.
     *
     * E.g. if a city expands while the player is away, it needs to tell the system to load buildings at the edge of
     * the city region without the centre of the city needing to be loaded (to force a simulation).
     */
    public float maxDelta;

    /**
     * The last time a {@link SectorSimulationEvent} was sent to this entity.
     *
     * This is used to calculate the delta between simulation events, and should not be changed outside of this class
     * or the {@link SectorSimulationSystem}.
     */
    protected float lastSimulationTime;

    /**
     * Create a new {@link SectorSimulationComponent} with the default max delta.
     */
    public SectorSimulationComponent() {
        new SectorSimulationComponent(MAX_DELTA_DEFAULT);
    }

    /**
     * @see SectorSimulationComponent#maxDelta
     */
    public SectorSimulationComponent(float maxDelta) {
        this.maxDelta = maxDelta;
    }
}
