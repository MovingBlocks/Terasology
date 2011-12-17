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
package com.github.begla.blockmania.world.simulators;

import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.generators.ChunkGeneratorTerrain;
import com.github.begla.blockmania.world.chunk.Chunk;
import com.github.begla.blockmania.world.interfaces.WorldProvider;

import javax.vecmath.Vector3f;

/**
 * Grows grass, flowers and high grass.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class GrowthSimulator extends Simulator {

    private static final Vector3f[] NEIGHBORS6 = {new Vector3f(0, -1, 0), new Vector3f(0, 1, 0), new Vector3f(-1, 0, 0), new Vector3f(1, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0, 0, -1)};
    private static final byte DIRT_TYPE = BlockManager.getInstance().getBlock("Dirt").getId();
    private static final byte GRASS_TYPE = BlockManager.getInstance().getBlock("Grass").getId();

    public GrowthSimulator(WorldProvider parent) {
        super(parent, 1000);
    }

    @Override
    public void executeSimulation() {

        if (_activeBlocks.isEmpty())
            return;

        BlockPosition pos = _activeBlocks.iterator().next();
        _activeBlocks.remove(pos);

        if (!_parent.canBlockSeeTheSky(pos.x, pos.y, pos.z))
            return;

        ChunkGeneratorTerrain.BIOME_TYPE biome = _parent.getActiveBiome(pos.x, pos.z);

        if (biome != ChunkGeneratorTerrain.BIOME_TYPE.SNOW) {
            byte bLeft = _parent.getBlock(pos.x - 1, pos.y, pos.z);
            byte bRight = _parent.getBlock(pos.x + 1, pos.y, pos.z);
            byte bUp = _parent.getBlock(pos.x, pos.y, pos.z + 1);
            byte bDown = _parent.getBlock(pos.x, pos.y, pos.z - 1);

            if (bLeft == GRASS_TYPE || bRight == GRASS_TYPE || bDown == GRASS_TYPE || bUp == GRASS_TYPE) {
                _parent.setBlock(pos.x, pos.y, pos.z, GRASS_TYPE, false, true);
            }

            if (bLeft == DIRT_TYPE) {
                addActiveBlock(new BlockPosition(pos.x - 1, pos.y, pos.z));
            }

            if (bRight == DIRT_TYPE) {
                addActiveBlock(new BlockPosition(pos.x + 1, pos.y, pos.z));
            }

            if (bUp == DIRT_TYPE) {
                addActiveBlock(new BlockPosition(pos.x, pos.y, pos.z + 1));
            }

            if (bDown == DIRT_TYPE) {
                addActiveBlock(new BlockPosition(pos.x, pos.y, pos.z - 1));
            }
        }
    }

    public void lightChanged(Chunk chunk, BlockPosition pos) {

    }

    public void blockPlaced(Chunk chunk, BlockPosition pos) {
        if (_parent.getBlock(pos.x, pos.y, pos.z) == DIRT_TYPE) {
            addActiveBlock(pos);
        }

        for (int i = 0; i < 6; i++) {
            BlockPosition nBp = new BlockPosition(pos.x + (int) NEIGHBORS6[i].x, pos.y + (int) NEIGHBORS6[i].y, pos.z + (int) NEIGHBORS6[i].z);

            if (_parent.getBlock(nBp.x, nBp.y, nBp.z) == DIRT_TYPE) {
                addActiveBlock(nBp);
            }
        }
    }

    public void blockRemoved(Chunk chunk, BlockPosition pos) {
        for (int i = 0; i < 6; i++) {
            BlockPosition nBp = new BlockPosition(pos.x + (int) NEIGHBORS6[i].x, pos.y + (int) NEIGHBORS6[i].y, pos.z + (int) NEIGHBORS6[i].z);

            if (_parent.getBlock(nBp.x, nBp.y, nBp.z) == DIRT_TYPE) {
                addActiveBlock(nBp);
            }
        }
    }
}

