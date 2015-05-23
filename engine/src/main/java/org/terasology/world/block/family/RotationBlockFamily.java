/*
 * Copyright 2015 MovingBlocks
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

import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.world.block.Block;

/**
 * Block family interface that defined its blocks by rotation.
 */
public interface RotationBlockFamily {
    /**
     * Returns a block from the family that is a clock-wise rotation transformation of the block passed in the parameter
     * where the "front" is the side specified in the parameter.
     * @param currentBlock Current block to transform.
     * @param sideToRotateAround Side to rotate around.
     * @return
     */
    Block getBlockForClockwiseRotation(Block currentBlock, Side sideToRotateAround);

    /**
     * Returns block from the block family for the specified rotation.
     * @param rotation
     * @return
     */
    Block getBlockForRotation(Rotation rotation);

    /**
     * Returns rotation used to create the specified block.
     * @param block
     * @return
     */
    Rotation getRotation(Block block);
}
