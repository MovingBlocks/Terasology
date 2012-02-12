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
package org.terasology.logic.manager;

import org.terasology.logic.world.IWorldProvider;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.blueprints.Blueprint;
import org.terasology.model.structures.BlockPosition;

import java.util.Collection;

/**
 * Provides the functionality to generate blueprints from a list of block positions.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlueprintManager {

    /* SINGLETON */
    public static BlueprintManager _instance;

    public static BlueprintManager getInstance() {
        if (_instance == null)
            _instance = new BlueprintManager();

        return _instance;
    }

    /**
     * Generates a blueprint from a set of blocks in the given world.
     *
     * @param provider       The world
     * @param blockPositions List of block positions
     * @return The final blueprint
     */
    public Blueprint generateBlueprint(IWorldProvider provider, Collection<BlockPosition> blockPositions) {
        Blueprint result = new Blueprint();

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;

        // Locate the origin of the rectangle
        for (BlockPosition pos : blockPositions) {
            if (pos.x < minX)
                minX = pos.x;
            if (pos.y < minY)
                minY = pos.y;
            if (pos.z < minZ)
                minZ = pos.z;
        }

        // Finally generate the blueprint
        for (BlockPosition pos : blockPositions) {
            BlockPosition newPos = new BlockPosition(pos.x - minX, pos.y - minY, pos.z - minZ);

            result.getBlockPositions().add(newPos);
            result.getBlockTypes().put(newPos, BlockManager.getInstance().getBlock(provider.getBlock(pos.x, pos.y, pos.z)));
        }

        return result;
    }

}
