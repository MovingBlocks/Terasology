// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.entity.placement;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.world.block.Block;
import org.terasology.math.geom.Vector3i;

import java.util.Collections;
import java.util.Map;

/**
 *
 */
public class PlaceBlocks extends AbstractConsumableEvent {
    private final Map<Vector3i, Block> blocks;
    private final EntityRef instigator;

    public PlaceBlocks(Vector3i location, Block block) {
        this(location, block, EntityRef.NULL);
    }

    public PlaceBlocks(Map<Vector3i, Block> blocks) {
        this(blocks, EntityRef.NULL);
    }

    public PlaceBlocks(Vector3i location, Block block, EntityRef instigator) {
        blocks = Collections.singletonMap(location, block);
        this.instigator = instigator;
    }

    public PlaceBlocks(Map<Vector3i, Block> blocks, EntityRef instigator) {
        this.blocks = blocks;
        this.instigator = instigator;
    }

    public Map<Vector3i, Block> getBlocks() {
        return Collections.unmodifiableMap(blocks);
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
