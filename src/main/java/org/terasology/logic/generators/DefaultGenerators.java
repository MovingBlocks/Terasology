/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.logic.generators;

import com.google.common.collect.ImmutableMap;
import org.terasology.engine.CoreRegistry;
import org.terasology.math.LSystemRule;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.generator.chunkGenerators.ForestGenerator;
import org.terasology.world.generator.chunkGenerators.TreeGenerator;
import org.terasology.world.generator.chunkGenerators.TreeGeneratorCactus;
import org.terasology.world.generator.chunkGenerators.TreeGeneratorLSystem;

import java.util.Map;

public class DefaultGenerators {

    public DefaultGenerators(ForestGenerator mngr) {

        BlockManager blockManager = CoreRegistry.get(BlockManager.class);

        Map<Character, LSystemRule> rules = ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFBFA]////[&BFFFA]////[&FBFFA]", 1.0f))
                .put('B', new LSystemRule("[&FFFA]////[&FFFA]////[&FFFA]", 0.8f)).build();
        TreeGenerator oakTree = new TreeGeneratorLSystem("FFFFFFA", rules, 4, (float) Math.toRadians(30)).setLeafType(blockManager.getBlock("engine:GreenLeaf"))
                .setBarkType(blockManager.getBlock("engine:OakTrunk")).setGenerationProbability(0.08f);

        // Pine
        rules = ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFFFFA]////[&FFFFFA]////[&FFFFFA]", 1.0f)).build();
        TreeGenerator pineTree = new TreeGeneratorLSystem("FFFFAFFFFFFFAFFFFA", rules, 4, (float) Math.toRadians(35)).setLeafType(blockManager.getBlock("engine:DarkLeaf"))
                .setBarkType(blockManager.getBlock("engine:PineTrunk")).setGenerationProbability(0.05f);

        // Birk
        rules = ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFFAFFF]////[&FFAFFF]////[&FFFAFFF]", 1.0f))
                .put('B', new LSystemRule("[&FAF]////[&FAF]////[&FAF]", 0.8f)).build();
        TreeGenerator birkTree = new TreeGeneratorLSystem("FFFFAFFFFBFFFFAFFFFBFFFFAFFFFBFF", rules, 4, (float) Math.toRadians(35)).setLeafType(blockManager.getBlock("engine:DarkLeaf"))
                .setBarkType(blockManager.getBlock("engine:BirkTrunk")).setGenerationProbability(0.02f);

        // Oak variation tree
        rules = ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFBFA]////[&BFFFA]////[&FBFFAFFA]", 1.0f))
                .put('B', new LSystemRule("[&FFFAFFFF]////[&FFFAFFF]////[&FFFAFFAA]", 0.8f)).build();
        TreeGenerator oakVariationTree = new TreeGeneratorLSystem("FFFFFFA", rules, 4, (float) Math.toRadians(35)).setLeafType(blockManager.getBlock("engine:GreenLeaf"))
                .setBarkType(blockManager.getBlock("engine:OakTrunk")).setGenerationProbability(0.08f);

        // A red tree
        rules = ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFAFF]////[&FFAFF]////[&FFAFF]", 1.0f)).build();
        TreeGenerator redTree = new TreeGeneratorLSystem("FFFFFAFAFAF", rules, 4, (float) Math.toRadians(40)).setLeafType(blockManager.getBlock("engine:RedLeaf"))
                .setBarkType(blockManager.getBlock("engine:OakTrunk")).setGenerationProbability(0.05f);

        // Cactus
        TreeGenerator cactus = new TreeGeneratorCactus().setTrunkType(blockManager.getBlock("engine:Cactus")).setGenerationProbability(0.05f);

        // Oak
        //TreeGenerator oakTree = new SeedTreeGenerator().setBlock(blockManager.getBlock("engine:OakSaplingGenerated")).setGenerationProbability(0.08f);

        // Add the trees to the generator lists
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.MOUNTAINS, oakTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.MOUNTAINS, pineTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.FOREST, oakTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.FOREST, pineTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.FOREST, oakVariationTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.SNOW, birkTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.PLAINS, redTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.PLAINS, oakTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.DESERT, cactus);
    }
}
