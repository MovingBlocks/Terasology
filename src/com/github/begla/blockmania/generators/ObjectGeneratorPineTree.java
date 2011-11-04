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

import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.main.BlockmaniaConfiguration;
import com.github.begla.blockmania.utilities.MathHelper;
import com.github.begla.blockmania.world.LocalWorldProvider;
import com.github.begla.blockmania.world.chunk.Chunk;

/**
 * Generates a simple pine tree.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ObjectGeneratorPineTree extends ObjectGenerator {

    /**
     * @param w
     * @param seed
     */
    public ObjectGeneratorPineTree(LocalWorldProvider w) {
        super(w);
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
        int height = MathHelper.fastAbs(_worldProvider.getRandom().randomInt() % 4) + 8;

        if (posY + height >= Chunk.getChunkDimensionY()) {
            return;
        }

        // Generate tree trunk
        for (int i = 0; i < height; i++) {
            _worldProvider.setBlock(posX, posY + i, posZ, BlockManager.getInstance().getBlock("Tree trunk").getId(), update, true);
        }

        int stage = 2;
        // Generate the treetop
        for (int y = height - 1; y >= (height * (1.0 / 3.0)); y--) {
            for (int x = -(stage / 2); x <= (stage / 2); x++) {
                for (int z = -(stage / 2); z <= (stage / 2); z++) {
                    if (!(x == 0 && z == 0)) {
                        _worldProvider.setBlock(posX + x, posY + y, posZ + z, BlockManager.getInstance().getBlock("Dark leaf").getId(), update, false);
                        _worldProvider.refreshSunlightAt(posX + x, posZ + z, false, true);
                    }
                }
            }

            stage++;
        }

        _worldProvider.setBlock(posX, posY + height, posZ, BlockManager.getInstance().getBlock("Dark leaf").getId(), update, false);
    }
}
