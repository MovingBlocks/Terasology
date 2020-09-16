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

import com.google.common.base.Preconditions;
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

    /**
     * The block position, at which the block is supposed to be placed at
     */
    public final Vector3ic blockPosition;

    /**
     * The block side, this block is being attached to, e.g. Top if the block is being placed on the ground
     */
    public final Side attachmentSide;

    /**
     * The player's viewing direction
     */
    public final Vector3fc viewingDirection;

    /**
     * The position on the block surface that the user aimed at when placing the block. A vector in the range (0..1, 0..1)
     */
    public final Vector2fc relativeAttachmentPosition;

    /**
     * @param blockPosition     The block position, at which the block is supposed to be placed at
     * @param attachmentSide    The side of the block which this block is being attached to, e.g. Top if the block is being placed on the ground
     * @param viewingDirection  The players viewing direction
     */
    public BlockPlacementData(Vector3ic blockPosition, Side attachmentSide, Vector3fc viewingDirection) {
        this(blockPosition, attachmentSide, viewingDirection, new Vector2f());
    }

    /**
     * @param blockPosition     The block position, at which the block is supposed to be placed at
     * @param attachmentSide    The side of the block which this block is being attached to, e.g. Top if the block is being placed on the ground
     * @param viewingDirection  The players viewing direction
     * @param relativeAttachmentPosition The position on the block surface that the user aimed at when placing the block. A vector in the range (0..1, 0..1)
     */
    public BlockPlacementData(Vector3ic blockPosition, Side attachmentSide, Vector3fc viewingDirection, Vector2fc relativeAttachmentPosition) {
        this.blockPosition = new Vector3i(Preconditions.checkNotNull(blockPosition));
        this.attachmentSide = Preconditions.checkNotNull(attachmentSide);
        this.viewingDirection = new Vector3f(Preconditions.checkNotNull(viewingDirection));
        this.relativeAttachmentPosition = new Vector2f(Preconditions.checkNotNull(relativeAttachmentPosition));
    }
}
