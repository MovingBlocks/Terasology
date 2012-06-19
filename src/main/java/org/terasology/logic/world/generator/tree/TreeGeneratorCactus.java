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
package org.terasology.logic.world.generator.tree;

import org.terasology.logic.world.WorldView;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.utilities.FastRandom;

/**
 * Cactus generator.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class TreeGeneratorCactus extends TreeGenerator {

    @Override
    public void generate(WorldView view, FastRandom rand, int posX, int posY, int posZ) {
        for (int y = posY; y < posY + 3; y++) {
            view.setBlock(posX, y, posZ, BlockManager.getInstance().getBlock("Cactus"), view.getBlock(posX, y, posZ));
        }
    }
}
