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
package com.github.begla.blockmania.blueprints;

import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.world.WorldProvider;
import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * TODO
 */
public class Blueprint {

    private FastList<BlockPosition> _blockPositions = FastList.newInstance();
    private FastMap<BlockPosition, Block> _blockTypes = FastMap.newInstance();

    public Blueprint() {

    }

    public void build(WorldProvider provider, BlockPosition position) {
        for (BlockPosition bp : _blockPositions) {
            provider.setBlock(bp.x + position.x, bp.y + position.y, bp.z + position.z, _blockTypes.get(bp).getId(), true, true);
        }
    }

    public FastList<BlockPosition> getBlockPositions() {
        return _blockPositions;
    }

    public FastMap<BlockPosition, Block> getBlockTypes() {
        return _blockTypes;
    }
}
