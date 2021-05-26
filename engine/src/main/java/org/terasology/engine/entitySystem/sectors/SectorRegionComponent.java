// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.sectors;

import org.joml.Vector3i;
import org.terasology.engine.entitySystem.Component;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.engine.world.chunks.Chunk;

import java.util.HashSet;
import java.util.Set;

/**
 * When this component is added to a sector-scope entity, the {@link SectorSimulationComponent} will count any chunks
 * listed in {@link #chunks} as watched chunks.
 *
 * {@link SectorUtil} contains helper methods for creating this component, and for modifying it using {@link Chunk},
 * rather than positions, if desired.
 */
@API
public class SectorRegionComponent implements Component {

    /**
     * The set of positions of chunks for this entity to watch.
     */
    public Set<Vector3i> chunks = new HashSet<>();

}
