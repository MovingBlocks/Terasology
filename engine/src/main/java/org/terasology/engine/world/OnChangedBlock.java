// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.world.block.Block;

/**
 * Event when a block has changed
 *
 */
public class OnChangedBlock implements Event {
    private Block oldType;
    private Block newType;
    private Vector3i blockPosition;

    public OnChangedBlock(Vector3ic pos, Block newType, Block oldType) {
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
