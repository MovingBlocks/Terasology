// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.items;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * This event gets called whenever a block item is placed in the world
 */
public class OnBlockItemPlaced implements Event {
    /**
     * The position where the block is placed
     */
    private Vector3i position = new Vector3i();

    /**
     * The entity corresponding to the placed block
     */
    private EntityRef placedBlock;

    /**
     * The entity which placed the block
     */
    private EntityRef instigator = EntityRef.NULL;

    /**
     *
     * @param pos the position that the block is placed
     * @param placedBlock the block that is placed
     * @param instigator the entity that places the block. A block placed without an instigator can be specified with {@link EntityRef#NULL}
     */
    public OnBlockItemPlaced(Vector3ic pos, EntityRef placedBlock, EntityRef instigator) {
        this.position.set(pos);
        this.placedBlock = placedBlock;
        this.instigator = instigator;
    }

    /**
     * @return world position of the placed block
     */
    public Vector3ic getPosition() {
        return position;
    }

    /**
     * @return The entity linked to the given block
     */
    public EntityRef getPlacedBlock() {
        return placedBlock;
    }

    /**
     * @return the entity that placed the block
     */
    public EntityRef getInstigator() {
        return instigator;
    }
}
