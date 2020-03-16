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

import org.joml.Vector3i;
import org.terasology.world.block.Block;

/**
 * Interface for Block family that gets updated by a change in a neighbor block.
 */
public interface UpdatesWithNeighboursFamily extends BlockFamily {
    /**
     * Update called when a neighbor block changes
     **/
    Block getBlockForNeighborUpdate(Vector3i location, Block oldBlock);
}
