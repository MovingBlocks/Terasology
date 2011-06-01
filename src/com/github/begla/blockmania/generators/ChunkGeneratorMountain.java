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
public class ChunkGeneratorMountain extends ChunkGeneratorTerrain {

    /**
     *
     * @param seed
     */
    public ChunkGeneratorMountain(String seed) {
        super(seed);
    }

    /**
     *
     * @param c
     */
    @Override
    public void generate(Chunk c) {
        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                int height = (int) (calcMountainIntensity(x + getOffsetX(c), z + getOffsetZ(c)) * 64f);

                int startY = -1;

                for (int i = (int) Configuration.CHUNK_DIMENSIONS.y; i >= 0; i--) {
                    Block b = Block.getBlockForType(c.getBlock(x, i, z));
                    if (b.getClass() == BlockGrass.class || b.getClass() == BlockDirt.class || b.getClass() == BlockSand.class) {
                        startY = i + 1;
                        break;
                    }
                }

                if (startY == -1) {
                    break;
                }

                boolean blockGenerated = false;
                for (int i = 0; i < height; i++) {
                    if (calcCanyonDensity(x + getOffsetX(c), i + getOffsetY(c) + startY, z + getOffsetZ(c)) < 0.05) {
                        if (i == height - 1) {
                            c.setBlock(x, i + startY, z, getBlockTailpiece(c, getBlockTypeForPosition(c, x, i + startY, z, 8), i + startY));
                        } else if (i < height - 1) {
                            c.setBlock(x, i + startY, z, getBlockTypeForPosition(c, x, i + startY, z, 8));
                        }
                        blockGenerated = true;
                    }
                }

                if (blockGenerated) {
                    if (c.getBlock(x, startY - 1, z) == 0x1) {
                        c.setBlock(x, startY - 1, z, (byte) 0x2);
                    }
                }
            }
        }
    }

    /**
     * Returns the detail level for the base terrain.
     * 
     * @param x
     * @param z
     * @return
     */
    public float calcMountainIntensity(float x, float z) {
        float result = 0.0f;
        result += _pGen3.noiseWithOctaves(0.01f * x, 0.01f, 0.01f * z, 8, 0.5f);


        result = (float) Math.sqrt(Math.abs(result));

        result -= 0.5f;

        if (result < 0f) {
            result = 0;
        }

        return result * 2f;
    }

    /**
     * Returns the canyon density for the base terrain.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    protected float calcCanyonDensity(float x, float y, float z) {
        float result = 0.0f;
        result += _pGen3.noiseWithOctaves2(0.02f * x, 0.02f * y, 0.02f * z, 3);
        return result;
    }
}
