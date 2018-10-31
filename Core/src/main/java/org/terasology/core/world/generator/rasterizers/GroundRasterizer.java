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
package org.terasology.core.world.generator.rasterizers;

import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.liquid.LiquidType;

/**
 */
public class GroundRasterizer implements WorldRasterizer {

    private Block stone;
    private Block water;

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        stone = blockManager.getBlock("core:stone");
        water = blockManager.getBlock("core:water");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        LiquidData waterLiquid = new LiquidData(LiquidType.WATER, LiquidData.MAX_LIQUID_DEPTH);
        SurfaceHeightFacet surfaceHeightData = chunkRegion.getFacet(SurfaceHeightFacet.class);
        Vector3i chunkOffset = chunk.getChunkWorldOffset();
        for (int x = 0; x < chunk.getChunkSizeX(); ++x) {
            for (int z = 0; z < chunk.getChunkSizeZ(); ++z) {
                float surfaceHeight = surfaceHeightData.get(x, z);
                int y;
                for (y = 0; y < chunk.getChunkSizeY() && y + chunkOffset.y < surfaceHeight; ++y) {
                    chunk.setBlock(x, y, z, stone);
                }
                for (; y < chunk.getChunkSizeY() && y + chunkOffset.y <= 32; ++y) {
                    chunk.setBlock(x, y, z, water);
                    chunk.setLiquid(x, y, z, waterLiquid);
                }
            }
        }
    }
}
