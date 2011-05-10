/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.github.begla.blockmania.generators;

import com.github.begla.blockmania.Chunk;
import com.github.begla.blockmania.Configuration;
import com.github.begla.blockmania.World;

/**
 * Generates some trees, flowers and wheat.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class GeneratorForest extends GeneratorTerrain {

    /**
     * Init. the forest generator.
     * 
     * @param seed
     */
    public GeneratorForest(String seed) {
        super(seed);
    }

    /**
     *
     * @param c
     * @param parent
     */
    @Override
    public void generate(Chunk c, World parent) {
        for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
            for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {

                    float dens = calcForestDensity(c.getBlockWorldPosX(x), c.getBlockWorldPosY(y), c.getBlockWorldPosZ(z));

                    // Generate grass
                    if (c.getBlock(x, y, z) == 0x1 && dens > 0.15) {
                        if (_rand.randomBoolean()) {
                            c.setBlock(x, y + 1, z, (byte) 0xB);
                        } else {
                            c.setBlock(x, y + 1, z, (byte) 0xC);
                        }
                    }

                    // Generate some flowers and wheat
                    if (c.getBlock(x, y, z) == 0x1 && dens > 0.5) {
                        if (_rand.randomDouble() > 0.25f) {
                            c.setBlock(x, y + 1, z, (byte) 0x9);
                        } else {
                            c.setBlock(x, y + 1, z, (byte) 0xA);
                        }
                    }

                    // Check the distance to the last placed trees
                    if (dens > 0.7 && c.getBlock(x, y, z) == 0x1 && y > 32) {
                        c.getParent().generatePineTree(c.getBlockWorldPosX(x), c.getBlockWorldPosY((int) y) + 1, c.getBlockWorldPosZ(z), false);
                    } else if (dens > 0.6f && c.getBlock(x, y, z) == 0x1 && y > 32) {
                        c.getParent().generateTree(c.getBlockWorldPosX(x), c.getBlockWorldPosY((int) y) + 1, c.getBlockWorldPosZ(z), false);
                    }
                }
            }
        }
    }

    /**
     * Returns the cave density for the base terrain.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    protected float calcForestDensity(float x, float y, float z) {
        float result = 0.0f;
        result += _pGen1.noise(0.8f * x, 0.8f * y, 0.8f * z);
        return result;
    }
}
