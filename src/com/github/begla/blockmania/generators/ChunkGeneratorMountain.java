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
                    float density = calcMountainDensity(x + getOffsetX(c), y + getOffsetY(c), z + getOffsetZ(c));

                    int height = (int) (128f * calcHeightMap(x + getOffsetX(c), z + getOffsetZ(c)));

                    if (y > height + 64 || y > 120) {
                        density /= (float) y;
                    }

                    densityMap[x][y][z] = density;
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

                boolean set = false;
                for (int y = (int) Configuration.CHUNK_DIMENSIONS.y; y >= 0; y--) {

                    float dens = densityMap[x][y][z];

                    if (c.getBlock(x, y, z) == 0x1) {
                        if (set) {
                            c.setBlock(x, y, z, (byte) 0x2);
                        }
                        break;
                    }

                    if (c.getBlock(x, y, z) == 0x2) {
                        break;
                    }

                    if ((dens > 0.05f && dens < 0.1f)) {
                        /*
                         * The outer layer is made of dirt and grass.
                         */
                        if (!set) {
                            c.setBlock(x, y, z, getBlockTailpiece(c, getBlockTypeForPosition(c, x, y, z, 1.0f), y));
                        } else {
                            c.setBlock(x, y, z, getBlockTypeForPosition(c, x, y, z, 1.0f));
                        }

                        set = true;
                    } else if (dens >= 0.1f) {
                        c.setBlock(x, y, z, getBlockTailpiece(c, getBlockTypeForPosition(c, x, y, z, 0.2f), y));
                        set = true;
                    }
                }
            }
        }
    }
}
