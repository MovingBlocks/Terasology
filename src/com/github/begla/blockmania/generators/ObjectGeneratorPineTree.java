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

import com.github.begla.blockmania.World;

/**
 * Generates a simple, bushy tree.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ObjectGeneratorPineTree extends ObjectGenerator {

    /**
     * 
     * @param w
     * @param seed
     */
    public ObjectGeneratorPineTree(World w, String seed) {
        super(w, seed);
    }

    /**
     * Generates the tree.
     *
     * @param posX Origin on the x-axis
     * @param posY Origin on the y-axis
     * @param posZ Origin on the z-axis
     */
    @Override
    public void generate(int posX, int posY, int posZ, boolean update) {
        int height = _rand.randomInt() % 4 + 12;

        // Generate tree trunk
        for (int i = 0; i < height; i++) {
            _world.setBlock(posX, posY + i, posZ, (byte) 0x5, update);
        }

        // Generate the treetop
        for (int y = 0; y < 10; y += 2) {
            for (int x = -5 + y / 2; x <= 5 - y / 2; x++) {
                for (int z = -5 + y / 2; z <= 5 - y / 2; z++) {
                    if (!(x == 0 && z == 0)) {
                        _world.setBlock(posX + x, posY + y + (height - 10), posZ + z, (byte) 0x6, update);
                    }
                }
            }
        }
    }
}
