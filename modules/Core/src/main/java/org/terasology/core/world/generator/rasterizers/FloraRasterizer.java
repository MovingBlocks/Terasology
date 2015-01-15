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

import java.util.List;
import java.util.Map;

import org.terasology.core.world.generator.facets.FloraFacet;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * @author Immortius
 */
public class FloraRasterizer implements WorldRasterizer {

    private final Map<FloraType, List<Block>> flora = Maps.newEnumMap(FloraType.class);

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);

        flora.put(FloraType.GRASS, ImmutableList.<Block>of(
                blockManager.getBlock("core:TallGrass1"),
                blockManager.getBlock("core:TallGrass2"),
                blockManager.getBlock("core:TallGrass3")));

        flora.put(FloraType.FLOWER, ImmutableList.<Block>of(
                blockManager.getBlock("core:Dandelion"),
                blockManager.getBlock("core:Glowbell"),
                blockManager.getBlock("core:Iris"),
                blockManager.getBlock("core:Lavender"),
                blockManager.getBlock("core:RedClover"),
                blockManager.getBlock("core:RedFlower"),
                blockManager.getBlock("core:Tulip"),
                blockManager.getBlock("core:YellowFlower")));

        flora.put(FloraType.MUSHROOM, ImmutableList.<Block>of(
                blockManager.getBlock("core:BigBrownShroom"),
                blockManager.getBlock("core:BrownShroom"),
                blockManager.getBlock("core:RedShroom")));
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        FloraFacet facet = chunkRegion.getFacet(FloraFacet.class);
        Block air = BlockManager.getAir();

        Random rng = new FastRandom(chunk.getPosition().hashCode());

        Map<Vector3i, FloraType> entries = facet.getRelativeEntries();
        for (Vector3i pos : entries.keySet()) {

            // check if some other rasterizer has already placed something here
            if (chunk.getBlock(pos).equals(air)) {

                FloraType type = entries.get(pos);
                Block block = getBlock(rng, type);
                chunk.setBlock(pos, block);
            }
        }
    }

    private Block getBlock(Random rng, FloraType type) {
        List<Block> list = flora.get(type);
        int blockIdx = rng.nextInt(list.size());
        Block block = list.get(blockIdx);
        return block;
    }
}
