/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.world.generator.tree;

import org.terasology.engine.CoreRegistry;
import org.terasology.utilities.procedural.FastRandom;
import org.terasology.world.ChunkView;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;

/**
 * Cactus generator.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class TreeGeneratorCactus extends TreeGenerator {

    private Block cactus;

    @Override
    public void generate(ChunkView view, FastRandom rand, int posX, int posY, int posZ) {
        for (int y = posY; y < posY + 3; y++) {
            view.setBlock(posX, y, posZ, cactus);
        }
    }

    @Override
    public TreeGenerator setBarkType(Block b) {
        cactus = b;
        return this;
    }
}
