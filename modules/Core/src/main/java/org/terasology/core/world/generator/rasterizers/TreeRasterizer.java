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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.chunkGenerators.TreeGenerator;
import org.terasology.core.world.generator.chunkGenerators.Trees;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.math.Vector3i;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

import java.util.Map;

/**
 * Creates trees based on the original
 */
public class TreeRasterizer implements WorldRasterizer {

    private Block tallGrass;

    @Override
    public void initialize() {
        // nothing to do
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        TreeFacet facet = chunkRegion.getFacet(TreeFacet.class);

        for (Map.Entry<Vector3i, TreeGenerator> entry : facet.getRelativeEntries().entrySet()) {
            Vector3i pos = entry.getKey();
            TreeGenerator treeGen = entry.getValue();
            treeGen.generate(chunk, new FastRandom(pos.hashCode()), pos.x, pos.y, pos.z);
        }
    }

}
