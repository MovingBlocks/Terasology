/*
 * Copyright 2018 MovingBlocks
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

import org.joml.Vector3ic;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;

/**
 * Interface for Block family that gets updated by a change in a neighbor block.
 */
public interface UpdatesWithNeighboursFamily extends BlockFamily {
    /**
     * Update called when a neighbor block changes
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #getBlockForNeighborUpdate(Vector3ic, Block)}.
     **/
    @Deprecated
    default Block getBlockForNeighborUpdate(Vector3i location, Block oldBlock) {
        return getBlockForNeighborUpdate(JomlUtil.from(location), oldBlock);
    }

    /**
     * Update the block when a neighbor changes
     *
     * @param location the location of the block
     * @param oldBlock the block before the neighbor was updated
     *
     * @return The block from the family to be placed
     */
    Block getBlockForNeighborUpdate(Vector3ic location, Block oldBlock);
}
