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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.terasology.core.world.generator.chunkGenerators.TreeGenerator;
import org.terasology.core.world.generator.chunkGenerators.TreeGeneratorCactus;
import org.terasology.core.world.generator.chunkGenerators.TreeGeneratorLSystem;
import org.terasology.core.world.generator.facets.TreeFacet;
import org.terasology.math.LSystemRule;
import org.terasology.math.Vector3i;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generation.facets.BiomeFacet;

import java.util.Map;

/**
 * Creates trees based on the original
 */
public class TreeRasterizer implements WorldRasterizer {

    private Block tallGrass;
    private Map<WorldBiomeProvider.Biome, TreeGenerator> treeGeneratorLookup = Maps.newHashMap();

    public TreeRasterizer(BlockManager blockManager) {

        Map<Character, LSystemRule> rules = ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFBFA]////[&BFFFA]////[&FBFFA]", 1.0f))
                .put('B', new LSystemRule("[&FFFA]////[&FFFA]////[&FFFA]", 0.8f)).build();
        TreeGenerator oakTree = new TreeGeneratorLSystem("FFFFFFA", rules, 4, (float) Math.toRadians(30)).setLeafType(blockManager.getBlock("core:GreenLeaf"))
                .setBarkType(blockManager.getBlock("core:OakTrunk")).setGenerationProbability(0.08f);

        // Pine
        rules = ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFFFFA]////[&FFFFFA]////[&FFFFFA]", 1.0f)).build();
        TreeGenerator pineTree = new TreeGeneratorLSystem("FFFFAFFFFFFFAFFFFA", rules, 4, (float) Math.toRadians(35)).setLeafType(blockManager.getBlock("core:DarkLeaf"))
                .setBarkType(blockManager.getBlock("core:PineTrunk")).setGenerationProbability(0.05f);

        // Birk
        rules = ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFFAFFF]////[&FFAFFF]////[&FFFAFFF]", 1.0f))
                .put('B', new LSystemRule("[&FAF]////[&FAF]////[&FAF]", 0.8f)).build();
        TreeGenerator birkTree = new TreeGeneratorLSystem("FFFFAFFFFBFFFFAFFFFBFFFFAFFFFBFF", rules, 4, (float) Math.toRadians(35))
                .setLeafType(blockManager.getBlock("core:DarkLeaf"))
                .setBarkType(blockManager.getBlock("core:BirkTrunk")).setGenerationProbability(0.02f);

        // Oak variation tree
        rules = ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFBFA]////[&BFFFA]////[&FBFFAFFA]", 1.0f))
                .put('B', new LSystemRule("[&FFFAFFFF]////[&FFFAFFF]////[&FFFAFFAA]", 0.8f)).build();
        TreeGenerator oakVariationTree = new TreeGeneratorLSystem("FFFFFFA", rules, 4, (float) Math.toRadians(35)).setLeafType(blockManager.getBlock("core:GreenLeaf"))
                .setBarkType(blockManager.getBlock("core:OakTrunk")).setGenerationProbability(0.08f);

        // A red tree
        rules = ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFAFF]////[&FFAFF]////[&FFAFF]", 1.0f)).build();
        TreeGenerator redTree = new TreeGeneratorLSystem("FFFFFAFAFAF", rules, 4, (float) Math.toRadians(40)).setLeafType(blockManager.getBlock("core:RedLeaf"))
                .setBarkType(blockManager.getBlock("core:OakTrunk")).setGenerationProbability(0.05f);

        // Cactus
        TreeGenerator cactus = new TreeGeneratorCactus().setTrunkType(blockManager.getBlock("core:Cactus")).setGenerationProbability(0.05f);

        // Add the trees to the generator lists
        treeGeneratorLookup.put(WorldBiomeProvider.Biome.MOUNTAINS, oakTree);
        treeGeneratorLookup.put(WorldBiomeProvider.Biome.MOUNTAINS, pineTree);

        treeGeneratorLookup.put(WorldBiomeProvider.Biome.FOREST, oakTree);
        treeGeneratorLookup.put(WorldBiomeProvider.Biome.FOREST, pineTree);
        treeGeneratorLookup.put(WorldBiomeProvider.Biome.FOREST, oakVariationTree);

        treeGeneratorLookup.put(WorldBiomeProvider.Biome.SNOW, birkTree);

        treeGeneratorLookup.put(WorldBiomeProvider.Biome.PLAINS, redTree);
        treeGeneratorLookup.put(WorldBiomeProvider.Biome.PLAINS, oakTree);

        treeGeneratorLookup.put(WorldBiomeProvider.Biome.DESERT, cactus);
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        TreeFacet facet = chunkRegion.getFacet(TreeFacet.class);
        BiomeFacet biomeFacet = chunkRegion.getFacet(BiomeFacet.class);

        for (Vector3i pos : ChunkConstants.CHUNK_REGION) {
            float facetValue = facet.get(pos);
            WorldBiomeProvider.Biome biome = biomeFacet.get(pos.getX(), pos.getZ());
            TreeGenerator generator = treeGeneratorLookup.get(biome);

            if (facetValue > 0 && generator.getGenerationProbability() < (facetValue / 256f)) {
                generator.generate(chunk, new FastRandom((long) facetValue), pos.getX(), pos.getY(), pos.getZ());
            }
        }
    }
}
