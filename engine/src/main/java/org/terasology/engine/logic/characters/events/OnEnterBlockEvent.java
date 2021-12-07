// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.events;

import org.joml.Vector3i;
import org.terasology.engine.world.block.Block;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This event gets sent to the player entity when it enters a new block.
 * For each block in the player's height, the OnEnterBlockEvent is sent.
 */
public class OnEnterBlockEvent implements Event {
    private Block oldBlock;
    private Block newBlock;
    private Vector3i characterRelativePosition;

    public OnEnterBlockEvent(Block oldBlock, Block newBlock, Vector3i characterRelativePosition) {
        this.oldBlock = oldBlock;
        this.newBlock = newBlock;
        this.characterRelativePosition = characterRelativePosition;
    }

    public Block getNewBlock() {
        return newBlock;
    }

    public Block getOldBlock() {
        return oldBlock;
    }

    public Vector3i getCharacterRelativePosition() {
        return characterRelativePosition;
    }
}
