/*
 * Copyright 2017 MovingBlocks
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

import org.terasology.biomesAPI.Biome;
import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceDepthFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

public class SolidRasterizer implements WorldRasterizer {

    private Block water;
    private Block ice;
    private Block stone;
    private Block sand;
    private Block grass;
    private Block snow;
    private Block dirt;
    private BiomeRegistry biomeRegistry;

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        biomeRegistry = CoreRegistry.get(BiomeRegistry.class);
        stone = blockManager.getBlock("CoreBlocks:Stone");
        water = blockManager.getBlock("CoreBlocks:Water");
        ice = blockManager.getBlock("CoreBlocks:Ice");
        sand = blockManager.getBlock("CoreBlocks:Sand");
        grass = blockManager.getBlock("CoreBlocks:Grass");
        snow = blockManager.getBlock("CoreBlocks:Snow");
        dirt = blockManager.getBlock("CoreBlocks:Dirt");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        DensityFacet solidityFacet = chunkRegion.getFacet(DensityFacet.class);
        SurfaceHeightFacet surfaceFacet = chunkRegion.getFacet(SurfaceHeightFacet.class);
        SurfaceDepthFacet surfaceDepthFacet = chunkRegion.getFacet(SurfaceDepthFacet.class);
        BiomeFacet biomeFacet = chunkRegion.getFacet(BiomeFacet.class);
        SeaLevelFacet seaLevelFacet = chunkRegion.getFacet(SeaLevelFacet.class);
        int seaLevel = seaLevelFacet.getSeaLevel();

        Vector2i pos2d = new Vector2i();
        for (Vector3i pos : ChunkConstants.CHUNK_REGION) {
            pos2d.set(pos.x, pos.z);
            int posY = pos.y + chunk.getChunkWorldOffsetY();

            // Check for an optional depth for this layer - if defined stop generating below that level
            if (surfaceDepthFacet != null && posY < surfaceDepthFacet.get(pos2d)) {
                continue;
            }

            Biome biome = biomeFacet.get(pos2d);
            biomeRegistry.setBiome(biome, chunk, pos.x, pos.y, pos.z);

            float density = solidityFacet.get(pos);

            if (density >= 32) {
                chunk.setBlock(pos, stone);
            } else if (density >= 0) {
                int depth = TeraMath.floorToInt(surfaceFacet.get(pos2d)) - posY;
                Block block = getSurfaceBlock(depth, posY,
                        biome,
                        seaLevel);
                chunk.setBlock(pos, block);
            } else {
                // fill up terrain up to sealevel height with water or ice
                if (posY == seaLevel && CoreBiome.SNOW == biome) {
                    chunk.setBlock(pos, ice);
                } else if (posY <= seaLevel) {         // either OCEAN or SNOW
                    chunk.setBlock(pos, water);
//                }
                }
            }
        }
    }

    private Block getSurfaceBlock(int depth, int height,
                                  Biome type,
                                  int seaLevel) {
        if (type instanceof CoreBiome) {
            switch ((CoreBiome) type) {
                case FOREST:
                case PLAINS:
                case MOUNTAINS:
                    // Beach
                    if (depth == 0 && height > seaLevel && height < seaLevel + 96) {
                        return grass;
                    } else if (depth == 0 && height >= seaLevel + 96) {
                        return snow;
                    } else if (depth > 32) {
                        return stone;
                    } else {
                        return dirt;
                    }
                case SNOW:
                    if (depth == 0 && height > seaLevel) {
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
                case OCEAN:
                    if (depth == 0) {
                        return sand;
                    } else {
                        return stone;
                    }
                case BEACH:
                    if (depth < 3) {
                        return sand;
                    } else {
                        return stone;
                    }
            }
        }
        return dirt;
    }
}
