// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block.items;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.math.JomlUtil;

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
     * @deprecated This is scheduled for removal in an upcoming version method will be replaced with JOML implementation
     *     {@link #OnBlockItemPlaced(Vector3ic, EntityRef, EntityRef)}.
     */
    @Deprecated
    public OnBlockItemPlaced(org.terasology.math.geom.Vector3i pos, EntityRef placedBlock) {
        this.position = JomlUtil.from(pos);
        this.placedBlock = placedBlock;
    }

    /**
     * @deprecated This is scheduled for removal in an upcoming version method will be replaced with JOML implementation
     *     {@link #OnBlockItemPlaced(Vector3ic, EntityRef, EntityRef)}.
     */
    @Deprecated
    public OnBlockItemPlaced(org.terasology.math.geom.Vector3i pos, EntityRef placedBlock, EntityRef instigator) {
        this.position = JomlUtil.from(pos);
        this.placedBlock = placedBlock;
        this.instigator = instigator;
    }

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
