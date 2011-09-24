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

import com.github.begla.blockmania.main.Configuration;
import com.github.begla.blockmania.world.LocalWorldProvider;

/**
 * A strange thing that looks like a magic tree.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Dominik Wilhelm
 */
public class ObjectGeneratorFirTree extends ObjectGenerator {

    /**
     * @param w
     * @param seed
     */
    public ObjectGeneratorFirTree(LocalWorldProvider w, String seed) {
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
        int height = Math.abs(_rand.randomInt() % 4) + 8;

        if (posY + height >= Configuration.CHUNK_DIMENSIONS.y) {
            return;
        }

        // Generate tree trunk
        for (int i = 0; i < height; i++) {
            _worldProvider.setBlock(posX, posY + i, posZ, (byte) 0x5, update, true);
        }

        int stage = 2;
        // Generate the treetop
        for (int y = height - 1; y >= (height * (1.0 / 3.0)); y--) {
            for (int x = -(stage / 2); x <= (stage / 2); x++) {
                if (!(x == 0)) {
                    _worldProvider.setBlock(posX + x, posY + y, posZ, (byte) 0x16, update, false);
                    _worldProvider.refreshSunlightAt(posX + x, 0, false, true);
                }
            }
            for (int z = -(stage / 2); z <= (stage / 2); z++) {
                if (!(z == 0)) {
                    _worldProvider.setBlock(posX, posY + y, posZ + z, (byte) 0x16, update, false);
                    _worldProvider.refreshSunlightAt(0, posZ + z, false, true);
                }
            }

            stage++;
        }

        _worldProvider.setBlock(posX, posY + height, posZ, (byte) 0x16, update, false);
    }
}
