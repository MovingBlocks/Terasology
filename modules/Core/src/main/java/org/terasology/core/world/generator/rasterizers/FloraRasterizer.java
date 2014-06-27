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

import org.terasology.core.world.generator.facets.PlantFacet;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generation.facets.SeaLevelFacet;

/**
 * @author Immortius
 */
public class FloraRasterizer implements WorldRasterizer {

    private Block tallGrass;

    public FloraRasterizer(BlockManager blockManager) {
        tallGrass = blockManager.getBlock("core:TallGrass1");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        PlantFacet facet = chunkRegion.getFacet(PlantFacet.class);
        SeaLevelFacet seaLevel = chunkRegion.getFacet(SeaLevelFacet.class);
        for (Vector3i pos : ChunkConstants.CHUNK_REGION) {
            if (pos.y + chunk.getChunkWorldOffsetY() > seaLevel.getSeaLevel() && facet.get(pos)) {
                chunk.setBlock(pos, tallGrass);
            }
        }
    }
}
