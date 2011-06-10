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
import com.github.begla.blockmania.Helper;
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

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                for (int i = (int) Configuration.CHUNK_DIMENSIONS.y - 1; i >= 0; i--) {
                    float height = (calcHeightMap(x + getOffsetX(c), z + getOffsetZ(c)) * 128f + 32f);

                    if (c.getBlock(x, i, z) == 0x1 || c.getBlock(x, i, z) == 0x2) {
                        break;
                    }

                    float dens = calcCanyonDensity(x + getOffsetX(c), i + getOffsetY(c), z + getOffsetZ(c));

                    float p = (float) i / height;

                    /*
                     * Reduce the density with growing height.
                     */
                    if (p > 0.6f && p < 1.0f) {
                        dens *= 1f - (p - 0.8f);
                    } else if (p >= 1.0) {
                        dens = 0f;
                    }

                    if (dens > 0.1f) {

                        if (c.canBlockSeeTheSky(x, i, z)) {
                            c.setBlock(x, i, z, getBlockTailpiece(c, getBlockTypeForPosition(c, x, i, z, (int) height), i));
                        } else {
                            c.setBlock(x, i, z, getBlockTypeForPosition(c, x, i, z, (int) height));
                        }
                    }

                }
            }
        }
    }
}
