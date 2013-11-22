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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.utilities.random.Random;
import org.terasology.world.ChunkViewAPI;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;

/**
 * Object generators are used to generate objects like trees etc.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class TreeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TreeGenerator.class);

    private float generationProbability = 1.0f;

    private Block grassBlock;
    private Block snowBlock;
    private Block sandBlock;

    public TreeGenerator() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        grassBlock = blockManager.getBlock("core:Grass");
        snowBlock = blockManager.getBlock("core:Snow");
        sandBlock = blockManager.getBlock("core:Sand");
    }

    /**
     * Generates a tree at the given position.
     *
     * @param view Chunk view
     * @param rand The random number generator
     * @param posX Position on the x-axis
     * @param posY Position on the y-axis
     * @param posZ Position on the z-axis
     */
    public abstract void generate(ChunkViewAPI view, Random rand, int posX, int posY, int posZ);

    public double getGenerationProbability() {
        return generationProbability;
    }

    public TreeGenerator setGenerationProbability(float genProbability) {
        this.generationProbability = genProbability;
        return this;
    }

    /**
     * Checks if a tree can grow at the given position
     *
     * @param view Chunk view
     * @param posX Position on the x-axis
     * @param posY Position on the y-axis
     * @param posZ Position on the z-axis
     */
    public boolean canGenerateAt(ChunkViewAPI view, int x, int y, int z) {
        Block posBlock = view.getBlock(x, y - 1, z);
        if (posBlock == null) {
            logger.error("WorldView.getBlock({}, {}, {}) return null, skipping forest generation (watchdog for issue #534)", x, y, z);
            return false;
        }

        if (!posBlock.equals(sandBlock) && !posBlock.equals(grassBlock) && !posBlock.equals(snowBlock)) {
            return false;
        }

        for (int checkY = y; checkY < Chunk.SIZE_Y; ++checkY) {
            if (!view.getBlock(x, checkY, z).isTranslucent()) {
                return false;
            }
        }

        return true;
    }
}
