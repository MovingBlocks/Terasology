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

package org.terasology.core.world.generator.trees;

import org.terasology.utilities.random.Random;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;

/**
 * Object generators are used to generate objects like trees etc.
 *
 */
public interface TreeGenerator {

    /**
     * Generates a tree at the given position.
     *
     * @param blockManager the block manager to resolve the block uris
     * @param view Chunk view
     * @param rand The random number generator
     * @param posX Relative position on the x-axis (wrt. the chunk)
     * @param posY Relative position on the y-axis (wrt. the chunk)
     * @param posZ Relative position on the z-axis (wrt. the chunk)
     */
    void generate(BlockManager blockManager, CoreChunk view, Random rand, int posX, int posY, int posZ);
}
