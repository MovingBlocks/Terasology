/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.generation.rasterizers;

import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generation.providers.SolidityProvider;

/**
 * @author Immortius
 */
public class SolidRasterizer implements WorldRasterizer {

    @Requires
    private SolidityProvider solidityProvider;

    private Block stone;
    private Block water;

    public SolidRasterizer(BlockManager blockManager) {
        stone = blockManager.getBlock("core:stone");
        water = blockManager.getBlock("core:water");
    }

    @Override
    public void generateChunk(CoreChunk chunk) {
        Vector3i chunkOffset = chunk.getChunkWorldOffset();
        boolean[] solids = solidityProvider.isSolid(chunk.getRegion());
        int solidIndex = 0;
        for (int z = 0; z < chunk.getChunkSizeZ(); ++z) {
            for (int y = 0; y < chunk.getChunkSizeY(); ++y) {
                for (int x = 0; x < chunk.getChunkSizeX(); ++x) {
                    boolean solid = solids[solidIndex++];
                    if (solid) {
                        chunk.setBlock(x, y, z, stone);
                    }
                    else if (y + chunkOffset.getY() < 32) {
                        chunk.setBlock(x, y, z, water);
                    }
                }
            }
        }
    }
}
