/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.block.family;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.math.Side;

/**
 * BlockPlacementData represents data that is useful for determining the orientation of new block.
 * The data is supposed to be derived from the players state when placing the block.
 * BlockPlacementData is immutable.
 */
public class BlockPlacementData {

    public final Vector3ic blockPosition;
    public final Side attachmentSide;
    public final Vector3fc viewingDirection;
    public final Vector2fc relativeAttachmentPosition;

    /**
     * @param blockPosition     The block position, at which the block is going to be placed at.
     * @param attachmentSide    The side of the block which this block is being attached to, e.g. Top if the block is being placed on the ground
     */
    public BlockPlacementData(Vector3ic blockPosition, Side attachmentSide, Vector3fc viewingDirection) {
        this(blockPosition, attachmentSide, viewingDirection, new Vector2f());
    }

    /**
     * @param blockPosition     The block position, at which the block is going to be placed at.
     * @param attachmentSide    The side of the block which this block is being attached to, e.g. Top if the block is being placed on the ground
     * @param relativeAttachmentPosition The position on the block surface that the user aimed at when placing the block. A value between (0, 0) and (1, 1).
     */
    public BlockPlacementData(Vector3ic blockPosition, Side attachmentSide, Vector3fc viewingDirection, Vector2fc relativeAttachmentPosition) {
        this.blockPosition = new Vector3i(blockPosition);
        this.attachmentSide = attachmentSide;
        this.viewingDirection = new Vector3f(viewingDirection);
        this.relativeAttachmentPosition = new Vector2f(relativeAttachmentPosition);
    }
}
