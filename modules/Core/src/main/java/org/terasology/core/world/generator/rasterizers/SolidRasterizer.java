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

import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * @author Immortius
 */
public class SolidRasterizer implements WorldRasterizer {

    private Block water;
    private Block ice;
    private Block stone;
    private Block sand;
    private Block grass;
    private Block snow;
    private Block dirt;

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        stone = blockManager.getBlock("core:stone");
        water = blockManager.getBlock("core:water");
        ice = blockManager.getBlock("core:Ice");
        sand = blockManager.getBlock("core:Sand");
        grass = blockManager.getBlock("core:Grass");
        snow = blockManager.getBlock("core:Snow");
        dirt = blockManager.getBlock("core:Dirt");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        DensityFacet solidityFacet = chunkRegion.getFacet(DensityFacet.class);
        SurfaceHeightFacet surfaceFacet = chunkRegion.getFacet(SurfaceHeightFacet.class);
        BiomeFacet biomeFacet = chunkRegion.getFacet(BiomeFacet.class);
        Vector2i pos2d = new Vector2i();
        for (Vector3i pos : ChunkConstants.CHUNK_REGION) {
            pos2d.set(pos.x, pos.z);
            CoreBiome biome = biomeFacet.get(pos2d);
            chunk.setBiome(pos.x, pos.y, pos.z, biome);

            float density = solidityFacet.get(pos);
            if (density >= 32) {
                chunk.setBlock(pos, stone);
            } else if (density >= 0) {
                int depth = TeraMath.floorToInt(surfaceFacet.get(pos2d)) - pos.y - chunk.getChunkWorldOffsetY();
                Block block = getSurfaceBlock(depth, pos.y + chunk.getChunkWorldOffsetY(), biome);
                chunk.setBlock(pos, block);
            } else {
                int posY = pos.y + chunk.getChunkWorldOffsetY();

                if (posY == 32 && CoreBiome.SNOW == biomeFacet.get(pos2d)) {
                    chunk.setBlock(pos, ice);
                } else if (posY <= 32) {
                    chunk.setBlock(pos, water);
                }
            }
        }
    }

    private Block getSurfaceBlock(int depth, int height, CoreBiome type) {
        switch (type) {
            case FOREST:
            case PLAINS:
            case MOUNTAINS:
                // Beach
                if (height >= 28 && height <= 34) {
                    return sand;
                } else if (depth == 0 && height > 32 && height < 128) {
                    return grass;
                } else if (depth == 0 && height >= 128) {
                    return snow;
                } else if (depth > 32) {
                    return stone;
                } else {
                    return dirt;
                }
            case SNOW:
                if (depth == 0.0 && height > 32) {
                    // Snow on top
                    return snow;
                } else if (depth > 32) {
                    // Stone
                    return stone;
                } else {
                    // Dirt
                    return dirt;
                }
            case DESERT:
                if (depth > 8) {
                    // Stone
                    return stone;
                } else {
                    return sand;
                }
        }
        return dirt;
    }
}
