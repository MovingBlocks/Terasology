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
package com.github.begla.blockmania.game.blueprints;

import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.world.interfaces.WorldProvider;

import java.util.HashMap;
import java.util.HashSet;

/**
 * A very simple prototype of the blueprint concept.
 * <p/>
 * Blueprints are currently a set of block positions and block types
 * that can be used to create replicas in the world.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Blueprint {

    private HashSet<BlockPosition> _blockPositions = new HashSet();
    private HashMap<BlockPosition, Block> _blockTypes = new HashMap();

    /**
     * Builds the blueprint in the given world at the given position.
     *
     * @param provider The world the blueprint should be build in
     * @param position The position the blueprint should be build
     */
    public void build(WorldProvider provider, BlockPosition position) {
        for (BlockPosition bp : _blockPositions) {
            provider.setBlock(bp.x + position.x, bp.y + position.y, bp.z + position.z, _blockTypes.get(bp).getId(), true, true);
        }
    }

    /**
     * Returns the list of block positions.
     *
     * @return The list
     */
    public HashSet<BlockPosition> getBlockPositions() {
        return _blockPositions;
    }

    /**
     * Returns the map assigning block types to block positions.
     *
     * @return The map
     */
    public HashMap<BlockPosition, Block> getBlockTypes() {
        return _blockTypes;
    }
}
