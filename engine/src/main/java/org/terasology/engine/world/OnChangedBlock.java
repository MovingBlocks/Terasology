// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.math.geom.Vector3i;
import org.terasology.engine.world.block.Block;

/**
 * Event when a block has changed
 *
 */
public class OnChangedBlock implements Event {
    private final Block oldType;
    private final Block newType;
    private final Vector3i blockPosition;

    public OnChangedBlock(Vector3i pos, Block newType, Block oldType) {
        this.blockPosition = new Vector3i(pos);
        this.oldType = oldType;
        this.newType = newType;
    }

    public Vector3i getBlockPosition() {
        return blockPosition;
    }

    public Block getOldType() {
        return oldType;
    }

    public Block getNewType() {
        return newType;
    }
}
