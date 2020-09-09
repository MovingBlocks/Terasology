// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

import gnu.trove.list.TIntList;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.block.internal.BlockPositionIterator;
import org.terasology.math.geom.Vector3i;

/**
 *
 */
public abstract class BlockLifecycleEvent implements Event {
    private final TIntList positions;
    private final BlockEntityRegistry registry;

    public BlockLifecycleEvent(TIntList positions, BlockEntityRegistry registry) {
        this.registry = registry;
        this.positions = positions;
    }

    public Iterable<Vector3i> getBlockPositions() {
        return () -> new BlockPositionIterator(positions, registry);
    }

    public int blockCount() {
        return positions.size();
    }
}
