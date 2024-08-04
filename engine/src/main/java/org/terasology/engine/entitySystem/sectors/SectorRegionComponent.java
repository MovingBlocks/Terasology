// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.sectors;

import org.joml.Vector3i;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * When this component is added to a sector-scope entity, the {@link SectorSimulationComponent} will count any chunks
 * listed in {@link #chunks} as watched chunks.
 *
 * {@link SectorUtil} contains helper methods for creating this component, and for modifying it using {@link Chunk},
 * rather than positions, if desired.
 */
@API
public class SectorRegionComponent implements Component<SectorRegionComponent> {

    /**
     * The set of positions of chunks for this entity to watch.
     */
    public Set<Vector3i> chunks = new HashSet<>();

    @Override
    public void copyFrom(SectorRegionComponent other) {
        this.chunks = other.chunks.stream()
                .map(Vector3i::new)
                .collect(Collectors.toSet());
    }
}
