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
package com.github.begla.blockmania.generators;

import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.configuration.ConfigurationManager;
import com.github.begla.blockmania.world.chunk.Chunk;

/**
 * Generates some basic resources.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkGeneratorResources extends ChunkGeneratorTerrain {

    public ChunkGeneratorResources(GeneratorManager generatorManager) {
        super(generatorManager);
    }

    @Override
    public void generate(Chunk c) {
        for (int x = 0; x < Chunk.getChunkDimensionX(); x++) {
            for (int z = 0; z < Chunk.getChunkDimensionZ(); z++) {
                for (int y = 0; y < Chunk.getChunkDimensionY(); y++) {
                    if (BlockManager.getInstance().getBlock(c.getBlock(x, y, z)).getTitle().equals("Stone")) {
                        if (_parent.getParent().getRandom().standNormalDistrDouble() < (Double) ConfigurationManager.getInstance().getConfig().get("World.Resources.probCoal")) {
                            c.setBlock(x, y, z, BlockManager.getInstance().getBlock("CoalOre").getId());
                        }

                        if (_parent.getParent().getRandom().standNormalDistrDouble() < (Double) ConfigurationManager.getInstance().getConfig().get("World.Resources.probGold")) {
                            c.setBlock(x, y, z, BlockManager.getInstance().getBlock("GoldOre").getId());
                        }

                        if (_parent.getParent().getRandom().standNormalDistrDouble() < (Double) ConfigurationManager.getInstance().getConfig().get("World.Resources.probDiamond")) {
                            c.setBlock(x, y, z, BlockManager.getInstance().getBlock("DiamondOre").getId());
                        }
                        if (_parent.getParent().getRandom().standNormalDistrDouble() < (Double) ConfigurationManager.getInstance().getConfig().get("World.Resources.probCopper")) {
                            c.setBlock(x, y, z, BlockManager.getInstance().getBlock("CopperOre").getId());
                        }

                        if (_parent.getParent().getRandom().standNormalDistrDouble() < (Double) ConfigurationManager.getInstance().getConfig().get("World.Resources.probIron")) {
                            c.setBlock(x, y, z, BlockManager.getInstance().getBlock("IronOre").getId());
                        }
                    }
                }
            }
        }
    }
}
