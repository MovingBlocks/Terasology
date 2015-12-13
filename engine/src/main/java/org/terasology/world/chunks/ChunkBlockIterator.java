/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.chunks;

import org.terasology.math.geom.Vector3i;
import org.terasology.module.sandbox.API;
import org.terasology.world.block.Block;

/**
 */
@API
public interface ChunkBlockIterator {

    /**
     * Updates the iterator to the next block
     *
     * @return True if a new block was found,
     */
    boolean next();

    /**
     * @return the current block
     */
    Block getBlock();

    /**
     * @return The world coords of the current block
     */
    Vector3i getBlockPos();
}
