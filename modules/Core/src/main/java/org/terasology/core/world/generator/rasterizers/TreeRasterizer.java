/*
 * Copyright 2015 MovingBlocks
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

import java.util.Map;

import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.core.world.generator.trees.TreeGenerator;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

/**
 * Creates trees based on the {@link TreeGenerator} that is
 * defined by the {@link TreeFacet}.
 * @author Martin Steiger
 */
public class TreeRasterizer implements WorldRasterizer {

    private BlockManager blockManager;

    @Override
    public void initialize() {
        blockManager = CoreRegistry.get(BlockManager.class);
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        TreeFacet facet = chunkRegion.getFacet(TreeFacet.class);

        for (Map.Entry<Vector3i, TreeGenerator> entry : facet.getRelativeEntries().entrySet()) {
            Vector3i pos = entry.getKey();
            TreeGenerator treeGen = entry.getValue();
            Random random = new FastRandom(pos.hashCode());
            treeGen.generate(blockManager, chunk, random, pos.x, pos.y, pos.z);
        }
    }

}
