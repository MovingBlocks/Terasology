/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

package com.github.begla.blockmania.generators;

import com.github.begla.blockmania.world.Chunk;
import com.github.begla.blockmania.Configuration;
import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.blocks.BlockDirt;
import com.github.begla.blockmania.blocks.BlockWater;

/**
 * Generates grass (substitutes dirt blocks), flowers and high grass.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorFlora extends ChunkGeneratorForest {

    /**
     * Init. the flora generator.
     * 
     * @param seed
     */
    public ChunkGeneratorFlora(String seed) {
        super(seed);
    }

    /**
     * Apply the generation process to the given chunk.
     * 
     * @param c
     */
    @Override
    public void generate(Chunk c) {
        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                for (int y = (int) Configuration.CHUNK_DIMENSIONS.y - 1; y >= 0; y--) {
                    byte type = c.getBlock(x, y, z);

                    // Ignore this column if a block was found, which is opaque and no dirt
                    if (Block.getBlockForType(type).getClass() != BlockDirt.class && !Block.getBlockForType(type).isBlockTypeTranslucent()) {
                        break;
                    }

                    // Do not generate flora under water
                    if (Block.getBlockForType(type).getClass() == BlockWater.class) {
                        break;
                    }

                    if (Block.getBlockForType(type).getClass() == BlockDirt.class) {
                        // Not every block should be updated each turn
                        if (_rand.randomDouble() > 0.025f) {
                            c.setBlock(x, y, z, (byte) 0x1);
                            generateGrassAndFlowers(c, x, y, z);
                        }
                        // Only update the topmost dirt block
                        break;
                    }
                }
            }
        }
    }
}
