// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.items;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.math.geom.Vector3i;

/**
 * This event gets called whenever a block item is placed in the world
 */
public class OnBlockItemPlaced implements Event {
    /**
     * The position where the block is placed
     */
    private final Vector3i position;

    /**
     * The entity corresponding to the placed block
     */
    private final EntityRef placedBlock;

    /**
     * The entity which placed the block
     */
    private EntityRef instigator = EntityRef.NULL;

    @Deprecated
    public OnBlockItemPlaced(Vector3i pos, EntityRef placedBlock) {
        this.position = pos;
        this.placedBlock = placedBlock;
    }

    public OnBlockItemPlaced(Vector3i pos, EntityRef placedBlock, EntityRef instigator) {
        this.position = pos;
        this.placedBlock = placedBlock;
        this.instigator = instigator;
    }

    public Vector3i getPosition() {
        return new Vector3i(position);
    }

    public EntityRef getPlacedBlock() {
        return placedBlock;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
