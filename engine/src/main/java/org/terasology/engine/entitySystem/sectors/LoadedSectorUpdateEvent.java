// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.sectors;

import org.joml.Vector3i;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.context.annotation.API;

import java.util.Set;

/**
 * This event will be sent by the {@link SectorSimulationSystem} to allow sector-scope entities to have an effect on the world,
 * whenever the chunk they are in is loaded.
 *
 * This event will always be immediately preceded by a {@link SectorSimulationEvent}, so no extra simulation needs to
 * take place for this event.
 */
@API
public class LoadedSectorUpdateEvent implements Event {

    /**
     * The set of positions of chunks which the sector is watching, and which are ready to be used.
     */
    private Set<Vector3i> readyChunks;

    /**
     * Create a new event with the given {@link #readyChunks}.
     *
     * @param readyChunks the readyChunks for the event.
     */
    public LoadedSectorUpdateEvent(Set<Vector3i> readyChunks) {
        this.readyChunks = readyChunks;
    }

    /**
     * @see #readyChunks
     */
    public Set<Vector3i> getReadyChunks() {
        return readyChunks;
    }
}
