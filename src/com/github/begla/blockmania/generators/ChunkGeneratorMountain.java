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
import com.github.begla.blockmania.blocks.BlockGrass;

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
        int xOffset = (int) c.getPosition().x * (int) Configuration.CHUNK_DIMENSIONS.x;
        int yOffset = (int) c.getPosition().y * (int) Configuration.CHUNK_DIMENSIONS.y;
        int zOffset = (int) c.getPosition().z * (int) Configuration.CHUNK_DIMENSIONS.z;

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                int height = (int) (calcMountainIntensity(x + xOffset, z + zOffset) * 32f);

                if (height == 0) {
                    break;
                }

                int startY = 0;

                for (int i = (int) Configuration.CHUNK_DIMENSIONS.y; i >= 0; i--) {
                    if (Block.getBlockForType(c.getBlock(x, i, z)).getClass() == BlockGrass.class) {
                        startY = i;
                        break;
                    }
                }

                for (int i = 0; i < height; i++) {
                    float stoneDensity = calcStoneDensity(x + xOffset, i + yOffset + startY, z + zOffset);
                    if (calcCanyonDensity(x + xOffset, i + yOffset + startY, z + zOffset) < 0.075) {
                        if (i == height - 1) {
                            // Generate grass on the top layer
                            if (c.canBlockSeeTheSky(x, i + startY, z)) {
                                c.setBlock(x, i + startY, z, (byte) 0x1);
                            } else {
                                c.setBlock(x, i + startY, z, (byte) 0x2);
                            }
                        } else if (i < height - 1) {
                            if (i < height * 0.75f && stoneDensity < 0.2f) {
                                // Generate the basic stone layer
                                c.setBlock(x, i + startY, z, (byte) 0x3);
                            } else {
                                // Fill the upper layer with dirt
                                c.setBlock(x, i + startY, z, (byte) 0x2);
                            }
                        }
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
    protected float calcMountainIntensity(float x, float z) {
        float result = 0.0f;
        result += _pGen1.noiseWithOctaves(0.01f * x, 0.01f, 0.01f * z, 12, 0.1f);


        result = (float) Math.abs(result);

        result -= 0.005f;

        if (result < 0f) {
            result = 0;
        }

        return result;
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
        result += _pGen3.noiseWithOctaves(0.05f * x, 0.05f * y, 0.05f * z, 12, 0.25f);
        return result;
    }
}
