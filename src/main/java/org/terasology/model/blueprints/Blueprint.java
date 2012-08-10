/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.model.blueprints;

import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.model.structures.BlockCollection;
import org.terasology.model.structures.BlockPosition;
import org.terasology.model.structures.BlockSelection;

/**
 * Blueprints are instructions for creating specific block structures in the world - relative positions of set blocks
 * Players (and creatures) can dynamically place blueprints of assorted types in-game creating the resulting product
 * Some blueprint types may additionally define selections of positions that are special for some reason (like Portals)
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public abstract class Blueprint {

    /**
     * The Blocks and which BlockPositions they exist at in this blueprint. Every Blueprint has this
     */
    protected BlockCollection _blockCollection = new BlockCollection();

    /**
     * Builds the blueprint in the given world at the given position, relative to the blueprint's attachment position
     *
     * @param provider The world the blueprint should be build in
     * @param pos      The position the blueprint should be built at
     * @return a BlockSelection containing localized positions for what was built
     */
    public BlockSelection build(WorldProvider provider, BlockPosition pos) {
        return _blockCollection.build(provider, pos);
    }

    /**
     * Returns the list of block positions.
     *
     * @return The list
     */
    public BlockSelection getBlockSelection() {
        return _blockCollection.positions();
    }

    public void addBlock(BlockPosition pos, Block b) {
        _blockCollection.addBlock(pos, b);
    }

    public BlockCollection getCollection() {
        return _blockCollection;
    }
}
