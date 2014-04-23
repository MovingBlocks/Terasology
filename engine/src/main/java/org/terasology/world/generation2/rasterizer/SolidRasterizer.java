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
package org.terasology.world.generation2.rasterizer;

import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation2.Region;
import org.terasology.world.generation2.WorldRasterizer;
import org.terasology.world.generation2.facets.Boolean3DIterator;
import org.terasology.world.generation2.facets.SolidityFacet;

/**
 * @author Immortius
 */
public class SolidRasterizer implements WorldRasterizer {

    private Block stone;
    private Block water;

    public SolidRasterizer(BlockManager blockManager) {
        stone = blockManager.getBlock("core:stone");
        water = blockManager.getBlock("core:water");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        SolidityFacet solidityFacet = chunkRegion.getFacet(SolidityFacet.class);
        Boolean3DIterator solidity = solidityFacet.get();
        while (solidity.hasNext()) {
            boolean solid = solidity.next();
            if (solid) {
                chunk.setBlock(solidity.currentPosition(), stone);
            } else if (solidity.currentPosition().getY() + chunk.getChunkWorldOffsetY() < 32) {
                chunk.setBlock(solidity.currentPosition(), water);
            }
        }
    }
}
