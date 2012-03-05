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
package org.terasology.logic.tools;

import org.terasology.logic.characters.Player;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.TeraMath;
import org.terasology.model.structures.BlockPosition;
import org.terasology.utilities.FastRandom;

/**
 * TODO
 */
public class SimpleTool implements ITool {

    protected static final FastRandom _random = new FastRandom();

    protected final Player _player;

    public SimpleTool(Player player) {
        _player = player;
    }

    public void executeLeftClickAction() {
    }

    public void executeRightClickAction() {
    }

    protected void placeBlock(int x, int y, int z, byte type, boolean update) {
        BlockPosition pos = new BlockPosition(x, y, z);
        placeBlock(pos, type, update);
    }

    protected void placeBlock(BlockPosition blockPos, byte type, boolean update) {
        IWorldProvider worldProvider = _player.getParent().getWorldProvider();

        // Set the block
        worldProvider.setBlock(blockPos.x, blockPos.y, blockPos.z, type, true, true);

        // Notify the world, that a block has been removed/placed
        int chunkPosX = TeraMath.calcChunkPosX(blockPos.x);
        int chunkPosZ = TeraMath.calcChunkPosZ(blockPos.z);

        if (type == 0) {
            _player.notifyObserversBlockRemoved(worldProvider.getChunkProvider().loadOrCreateChunk(chunkPosX, chunkPosZ), blockPos, update);
        } else {
            _player.notifyObserversBlockPlaced(worldProvider.getChunkProvider().loadOrCreateChunk(chunkPosX, chunkPosZ), blockPos, update);
        }
    }
}
