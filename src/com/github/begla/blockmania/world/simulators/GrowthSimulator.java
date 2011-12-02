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
import com.github.begla.blockmania.utilities.MathHelper;
import com.github.begla.blockmania.world.chunk.Chunk;
import com.github.begla.blockmania.world.interfaces.WorldProvider;

import java.util.Collection;

/**
 * Grows grass, flowers and high grass.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class GrowthSimulator extends Simulator {

    private static final byte DIRT_TYPE = BlockManager.getInstance().getBlock("Dirt").getId();
    private static final byte GRASS_TYPE = BlockManager.getInstance().getBlock("Grass").getId();

    public GrowthSimulator(WorldProvider parent) {
        super(parent, 1000);
    }

    @Override
    public void executeSimulation() {
        // TODO
    }

    private void growGrass(Chunk c) {
        for (int i = 0; i < 256; i++) {

            int x = Math.abs(_parent.getRandom().randomInt()) % Chunk.getChunkDimensionX();
            int z = Math.abs(_parent.getRandom().randomInt()) % Chunk.getChunkDimensionZ();

            for (int y = Chunk.getChunkDimensionY() - 1; y >= 0; y--) {

                byte type = _parent.getBlock(c.getBlockWorldPosX(x), y, c.getBlockWorldPosZ(z));
                BlockPosition pos = new BlockPosition(c.getBlockWorldPosX(x), y, c.getBlockWorldPosZ(z));

                if (type == DIRT_TYPE) {

                    ChunkGeneratorTerrain.BIOME_TYPE biome = _parent.getActiveBiome(pos.x, pos.z);

                    if (biome != ChunkGeneratorTerrain.BIOME_TYPE.SNOW) {

                        byte bLeft = _parent.getBlock(pos.x + 1, pos.y, pos.z);
                        byte bRight = _parent.getBlock(pos.x - 1, pos.y, pos.z);
                        byte bUp = _parent.getBlock(pos.x, pos.y, pos.z + 1);
                        byte bDown = _parent.getBlock(pos.x, pos.y, pos.z - 1);

                        if (bLeft == GRASS_TYPE || bRight == GRASS_TYPE || bDown == GRASS_TYPE || bUp == GRASS_TYPE) {
                            _parent.setBlock(pos.x, pos.y, pos.z, GRASS_TYPE, false, false, true);

                            _activeChunks.add(c);
                            for (Chunk n : c.loadOrCreateNeighbors()) {
                                _activeChunks.add(n);
                            }
                            return;
                        }
                    }
                } else if (type != 0x0) {
                    break;
                }

            }
        }

        _activeChunks.remove(c);
    }
}

