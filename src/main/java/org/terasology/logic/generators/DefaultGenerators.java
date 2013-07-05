/*
 * Copyright 2013 Moving Blocks
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

// Just cheating - temp internal version of the file in the external groovy dir
// TODO: Find a nice generic way of embedding the tree rules into the relevant block definition for said tree

package org.terasology.logic.generators;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.terasology.engine.CoreRegistry;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.generator.core.ForestGenerator;
import org.terasology.world.generator.tree.TreeGenerator;
import org.terasology.world.generator.tree.TreeGeneratorCactus;
import org.terasology.world.generator.tree.TreeGeneratorLSystem;

import java.util.Map;

public class DefaultGenerators {

    public DefaultGenerators(ForestGenerator mngr) {

        BlockManager blockManager = CoreRegistry.get(BlockManager.class);

        Map<String, Double> probs = Maps.newHashMap();
        probs.put("A", 1.0);
        probs.put("B", 0.8);

        Map<String, String> rules = ImmutableMap.<String, String>builder()
                .put("A", "[&FFBFA]////[&BFFFA]////[&FBFFA]")
                .put("B", "[&FFFA]////[&FFFA]////[&FFFA]").build();
        TreeGenerator oakTree = new TreeGeneratorLSystem("FFFFFFA", rules, probs, 4, 30).setLeafType(blockManager.getBlock("engine:GreenLeaf")).setBarkType(blockManager.getBlock("engine:OakTrunk")).setGenerationProbability(0.08f);

        // Pine
        rules = ImmutableMap.<String, String>builder()
                .put("A", "[&FFFFFA]////[&FFFFFA]////[&FFFFFA]").build();
        TreeGenerator pineTree = new TreeGeneratorLSystem("FFFFAFFFFFFFAFFFFA", rules, probs, 4, 35).setLeafType(blockManager.getBlock("engine:DarkLeaf")).setBarkType(blockManager.getBlock("engine:PineTrunk")).setGenerationProbability(0.05f);

        // Birk
        rules = ImmutableMap.<String, String>builder()
                .put("A", "[&FFFAFFF]////[&FFAFFF]////[&FFFAFFF]")
                .put("B", "[&FAF]////[&FAF]////[&FAF]").build();
        TreeGenerator birkTree = new TreeGeneratorLSystem("FFFFAFFFFBFFFFAFFFFBFFFFAFFFFBFF", rules, probs, 4, 35).setLeafType(blockManager.getBlock("engine:DarkLeaf")).setBarkType(blockManager.getBlock("engine:BirkTrunk")).setGenerationProbability(0.02f);

        // Oak variation tree
        rules = ImmutableMap.<String, String>builder()
                .put("A", "[&FFBFA]////[&BFFFA]////[&FBFFAFFA]")
                .put("B", "[&FFFAFFFF]////[&FFFAFFF]////[&FFFAFFAA]").build();
        TreeGenerator oakVariationTree = new TreeGeneratorLSystem("FFFFFFA", rules, probs, 4, 35).setLeafType(blockManager.getBlock("engine:GreenLeaf")).setBarkType(blockManager.getBlock("engine:OakTrunk")).setGenerationProbability(0.08f);

        // A red tree
        rules = ImmutableMap.<String, String>builder()
                .put("A", "[&FFAFF]////[&FFAFF]////[&FFAFF]").build();
        TreeGenerator redTree = new TreeGeneratorLSystem("FFFFFAFAFAF", rules, probs, 4, 40).setLeafType(blockManager.getBlock("engine:RedLeaf")).setBarkType(blockManager.getBlock("engine:OakTrunk")).setGenerationProbability(0.05f);

        // Cactus
        TreeGenerator cactus = new TreeGeneratorCactus().setBarkType(blockManager.getBlock("engine:Cactus")).setGenerationProbability(0.05f);

        // Add the trees to the generator lists
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.MOUNTAINS, oakTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.MOUNTAINS, pineTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.MOUNTAINS, redTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.FOREST, oakTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.FOREST, pineTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.FOREST, oakVariationTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.SNOW, birkTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.PLAINS, redTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.PLAINS, oakTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.DESERT, cactus);
    }
}