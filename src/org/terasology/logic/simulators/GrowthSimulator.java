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
package org.terasology.logic.simulators;

import org.terasology.logic.generators.ChunkGeneratorTerrain;
import org.terasology.logic.world.Chunk;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.BlockPosition;

import javax.vecmath.Vector3d;

/**
 * Grows grass, flowers and high grass.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class GrowthSimulator extends Simulator {

    private static final Vector3d[] NEIGHBORS6 = {new Vector3d(0, -1, 0), new Vector3d(0, 1, 0), new Vector3d(-1, 0, 0), new Vector3d(1, 0, 0), new Vector3d(0, 0, 1), new Vector3d(0, 0, -1)};
    private static final byte DIRT_TYPE = BlockManager.getInstance().getBlock("Dirt").getId();
    private static final byte GRASS_TYPE = BlockManager.getInstance().getBlock("Grass").getId();

    public GrowthSimulator(IWorldProvider parent) {
        super("Growth", parent, 1000);
    }

    @Override
    public boolean executeSimulation() {
        BlockPosition blockPos = tryRemoveFirstBlock();

        if (blockPos != null) {
            if (!_parent.canBlockSeeTheSky(blockPos.x, blockPos.y, blockPos.z))
                return false;

            ChunkGeneratorTerrain.BIOME_TYPE biome = _parent.getActiveBiome(blockPos.x, blockPos.z);

            if (biome != ChunkGeneratorTerrain.BIOME_TYPE.SNOW) {
                byte bLeft = _parent.getBlock(blockPos.x - 1, blockPos.y, blockPos.z);
                byte bRight = _parent.getBlock(blockPos.x + 1, blockPos.y, blockPos.z);
                byte bUp = _parent.getBlock(blockPos.x, blockPos.y, blockPos.z + 1);
                byte bDown = _parent.getBlock(blockPos.x, blockPos.y, blockPos.z - 1);

                if (bLeft == GRASS_TYPE || bRight == GRASS_TYPE || bDown == GRASS_TYPE || bUp == GRASS_TYPE) {
                    // TODO: don't suppress, but instead ignore updates from self?
                    _parent.setBlock(blockPos.x, blockPos.y, blockPos.z, GRASS_TYPE, false, true, true);
                }

                if (bLeft == DIRT_TYPE) {
                    addActiveBlock(new BlockPosition(blockPos.x - 1, blockPos.y, blockPos.z));
                }

                if (bRight == DIRT_TYPE) {
                    addActiveBlock(new BlockPosition(blockPos.x + 1, blockPos.y, blockPos.z));
                }

                if (bUp == DIRT_TYPE) {
                    addActiveBlock(new BlockPosition(blockPos.x, blockPos.y, blockPos.z + 1));
                }

                if (bDown == DIRT_TYPE) {
                    addActiveBlock(new BlockPosition(blockPos.x, blockPos.y, blockPos.z - 1));
                }
            }

            return true;
        }

        return false;
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

