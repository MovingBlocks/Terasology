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

import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.blocks.BlockStone;
import com.github.begla.blockmania.main.Configuration;
import com.github.begla.blockmania.world.LocalWorldProvider;
import com.github.begla.blockmania.world.chunk.Chunk;

/**
 * Generates some basic resources.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorResources extends ChunkGeneratorTerrain {

    /**
     * @param seed
     */
    public ChunkGeneratorResources(LocalWorldProvider worldProvider) {
        super(worldProvider);
    }

    /**
     * @param c
     */
    @Override
    public void generate(Chunk c) {
        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
                    if (Block.getBlockForType(c.getBlock(x, y, z)).getClass() == BlockStone.class) {
                        if (_worldProvider.getRandom().standNormalDistrDouble() < Configuration.PROB_COAL) {
                            c.setBlock(x, y, z, (byte) 0x14);
                        }

                        if (_worldProvider.getRandom().standNormalDistrDouble() < Configuration.PROB_GOLD) {
                            c.setBlock(x, y, z, (byte) 0x15);
                        }

                        if (_worldProvider.getRandom().standNormalDistrDouble() < Configuration.PROB_DIAMOND) {
                            c.setBlock(x, y, z, (byte) 35);
                        }
                        if (_worldProvider.getRandom().standNormalDistrDouble() < Configuration.PROB_REDSTONE) {
                            c.setBlock(x, y, z, (byte) 33);
                        }

                        if (_worldProvider.getRandom().standNormalDistrDouble() < Configuration.PROB_SILVER) {
                            c.setBlock(x, y, z, (byte) 34);
                        }
                    }
                }
            }
        }
    }
}
