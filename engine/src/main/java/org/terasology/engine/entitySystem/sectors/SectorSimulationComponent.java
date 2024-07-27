// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.sectors;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.internal.BaseEntityRef;
import org.terasology.engine.entitySystem.entity.internal.EntityScope;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;

/**
 * The component that allows the {@link SectorSimulationSystem} to send simulation events to a sector-scope entity.
 *
 * This should be automatically added by either {@link EntityManager#createSectorEntity(long)} or
 * {@link BaseEntityRef#setScope(EntityScope)}, so modules should not need to modify or add it.
 */
@API
public class SectorSimulationComponent implements Component<SectorSimulationComponent> {

    public static final long UNLOADED_MAX_DELTA_DEFAULT = 60_000;
    public static final long LOADED_MAX_DELTA_DEFAULT = 10_000;

    /**
     * The maximum time that can elapse between {@link SectorSimulationEvent}s being sent, in ms, when none of the
     * entity's watched chunks are loaded. This value does not change the fact that a simulation event is always sent
     * when the chunk the entity is in is loaded.
     *
     * This should be set as high as possible, so that fewer simulation events need to be sent in total. The purpose of
     * this value is to allow checking for whether its borders need to be expanded regularly, so that the appropriate
     * events are called if those expanded regions are loaded.
     *
     * E.g. if a city expands while the player is away, it needs to tell the system to load buildings at the edge of
     * the city region without the centre of the city needing to be loaded (to force a simulation).
     */
    public long unloadedMaxDelta;

    /**
     * The maximum time that can elapse between {@link SectorSimulationEvent}s being sent, in ms, when at least one of
     * this entity's watched chunks is loaded.
     */
    public long loadedMaxDelta;

    /**
     * The last time (game time, in ms) a {@link SectorSimulationEvent} was sent to this entity.
     *
     * This is used to calculate the delta between simulation events, and should not be changed outside of this class
     * or the {@link SectorSimulationSystem}.
     */
    public long lastSimulationTime;

    /**
     * Create a new {@link SectorSimulationComponent} with the default max delta.
     */
    public SectorSimulationComponent() {
        unloadedMaxDelta = UNLOADED_MAX_DELTA_DEFAULT;
        loadedMaxDelta = LOADED_MAX_DELTA_DEFAULT;
    }

    @Override
    public void copyFrom(SectorSimulationComponent other) {
        this.lastSimulationTime = other.lastSimulationTime;
        this.loadedMaxDelta = other.loadedMaxDelta;
        this.unloadedMaxDelta = other.unloadedMaxDelta;
    }
}
