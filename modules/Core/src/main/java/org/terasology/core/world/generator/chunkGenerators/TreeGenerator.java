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

package org.terasology.core.world.generator.chunkGenerators;

import org.terasology.utilities.random.Random;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.CoreChunk;

/**
 * Object generators are used to generate objects like trees etc.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class TreeGenerator {

    /**
     * Generates a tree at the given position.
     *
     * @param view Chunk view
     * @param rand The random number generator
     * @param posX Position on the x-axis
     * @param posY Position on the y-axis
     * @param posZ Position on the z-axis
     */
    public abstract void generate(CoreChunk view, Random rand, int posX, int posY, int posZ);

    protected void safelySetBlock(CoreChunk chunk, int x, int y, int z, Block block) {
        if (ChunkConstants.CHUNK_REGION.encompasses(x, y, z)) {
            chunk.setBlock(x, y, z, block);
        }
    }
}
