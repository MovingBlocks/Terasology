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
import com.github.begla.blockmania.utilities.Helper;
import com.github.begla.blockmania.blocks.Block;

/**
 * Generates the base terrain of the world.
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
        float[][][] densityMap = new float[(int) Configuration.CHUNK_DIMENSIONS.x + 1][(int) Configuration.CHUNK_DIMENSIONS.y + 1][(int) Configuration.CHUNK_DIMENSIONS.z + 1];

        /*
         * Create the density map at a lower sample rate.
         */
        for (int x = 0; x <= Configuration.CHUNK_DIMENSIONS.x; x += SAMPLE_RATE_3D_HOR) {
            for (int z = 0; z <= Configuration.CHUNK_DIMENSIONS.z; z += SAMPLE_RATE_3D_HOR) {
                for (int y = 0; y <= Configuration.CHUNK_DIMENSIONS.y; y += SAMPLE_RATE_3D_VERT) {
                    densityMap[x][y][z] = calcMountainDensity(x + getOffsetX(c), y + getOffsetY(c), z + getOffsetZ(c));
                }
            }
        }

        /*
         * Trilinear interpolate the missing values.
         */
        triLerpDensityMap(densityMap);

        /*
         * Generate the chunk from the density map.
         */
        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                for (int y = 100; y >= 0; y--) {
                    float height = (calcHeightMap(x + getOffsetX(c), z + getOffsetZ(c)) * 128f + 32f);

                    if (height < 0) {
                        break;
                    }

                    if (height > 100f) {
                        height = 100f;
                    }

                    float dens = densityMap[x][y][z];
                    float p = (float) y / height;


                    if (c.getBlock(x, y, z) == 0x1 || c.getBlock(x, y, z) == 0x2) {
                        break;
                    }

                    /*
                     * Reduce the density with growing height.
                     */
                    if (p > 0.6f && p < 1.0f) {
                        dens *= 1f - (p - 0.6f);
                    } else if (p >= 1.0) {
                        dens = 0f;
                    }

                    if ((dens > 0.06f && dens < 0.1f) || (dens >= 0.1f && p >= 0.95f)) {
                        /*
                         * The outer layer is made of dirt and grass.
                         */
                        if (c.canBlockSeeTheSky(x, y, z)) {
                            c.setBlock(x, y, z, getBlockTailpiece(c, getBlockTypeForPosition(c, x, y, z, 1.0f), y));
                        } else {
                            c.setBlock(x, y, z, getBlockTypeForPosition(c, x, y, z, 1.0f));
                        }
                    } else if (dens >= 0.1f && p < 0.95f) {
                        /*
                         * The inner layer is made of stone. But only if the height is < 90 %.
                         */
                        if (c.canBlockSeeTheSky(x, y, z)) {
                            c.setBlock(x, y, z, getBlockTailpiece(c, getBlockTypeForPosition(c, x, y, z, 0.2f), y));
                        } else {
                            c.setBlock(x, y, z, getBlockTypeForPosition(c, x, y, z, 0.2f));
                        }
                    }
                }
            }
        }
    }
}
