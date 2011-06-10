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
import com.github.begla.blockmania.blocks.BlockGrass;
import com.github.begla.blockmania.blocks.BlockSand;

/**
 * TODO
 * 
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorLakes extends ChunkGeneratorTerrain {

    /**
     *
     * @param seed
     */
    public ChunkGeneratorLakes(String seed) {
        super(seed);
    }

    /**
     *
     * @param c
     */
    @Override
    public void generate(Chunk c) {
        int xOffset = (int) c.getPosition().x * (int) Configuration.CHUNK_DIMENSIONS.x;
        int zOffset = (int) c.getPosition().z * (int) Configuration.CHUNK_DIMENSIONS.z;

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                float l = calcLakeIntensity(x + xOffset, z + zOffset);
                int lDepth = 0;

                if (l < 0.2f) {
                    lDepth = (int) ((1f - l) * 8f);
                }

                int startY = 0;

                for (int i = (int) Configuration.CHUNK_DIMENSIONS.y; i >= 0; i--) {
                    Class block = Block.getBlockForType(c.getBlock(x, i, z)).getClass();
                    if (block == BlockGrass.class || block == BlockSand.class || block == BlockDirt.class) {
                        startY = i;
                        break;
                    }
                }

                if (startY < 32) {
                    break;
                }

                for (int i = 0; i < lDepth; i++) {
                    if (i > 1) {
                        c.setBlock(x, startY - i, z, (byte) 0x04);
                    } else {
                        c.setBlock(x, startY - i, z, (byte) 0x00);
                    }
                }
            }
        }
    }

    /**
     * 
     * @param x
     * @param z
     * @return 
     */
    protected float calcLakeIntensity(float x, float z) {
        float result = 0.0f;
        result += _pGen3.noiseWithOctaves(x * 0.01f, 0.01f, 0.01f * z, 12, 1.9f, 5f);
        return (float) Math.sqrt(Math.abs(result));
    }
}
