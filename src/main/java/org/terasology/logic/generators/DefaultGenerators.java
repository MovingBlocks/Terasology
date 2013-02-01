// Just cheating - temp internal version of the file in the external groovy dir
// TODO: Find a nice generic way of embedding the tree rules into the relevant block definition for said tree

package org.terasology.logic.generators;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.generator.tree.TreeGenerator;
import org.terasology.world.generator.tree.TreeGeneratorLSystem;
import org.terasology.world.generator.tree.TreeGeneratorCactus;
import org.terasology.world.generator.core.ForestGenerator;

import org.terasology.world.WorldBiomeProvider;

import java.util.Map;

public class DefaultGenerators {

    public DefaultGenerators(ForestGenerator mngr) {

        Map<String, Double> probs = Maps.newHashMap();
        probs.put("A", 1.0);
        probs.put("B", 0.8);

        Map<String, String> rules = ImmutableMap.<String, String>builder()
                .put("A", "[&FFBFA]////[&BFFFA]////[&FBFFA]")
                .put("B", "[&FFFA]////[&FFFA]////[&FFFA]").build();
        TreeGenerator oakTree = new TreeGeneratorLSystem("FFFFFFA", rules, probs, 4, 30).setGenerationProbability(0.08f);

        // Pine
        rules = ImmutableMap.<String, String>builder()
            .put("A", "[&FFFFFA]////[&FFFFFA]////[&FFFFFA]").build();
        TreeGenerator pineTree = new TreeGeneratorLSystem("FFFFAFFFFFFFAFFFFA", rules, probs, 4, 35).setLeafType(BlockManager.getInstance().getBlock("engine:DarkLeaf")).setGenerationProbability(0.05f).setBarkType(BlockManager.getInstance().getBlock("engine:PineTrunk"));

        // Birk
        rules = ImmutableMap.<String, String>builder()
            .put("A", "[&FFFAFFF]////[&FFAFFF]////[&FFFAFFF]")
            .put("B", "[&FAF]////[&FAF]////[&FAF]").build();
        TreeGenerator birkTree = new TreeGeneratorLSystem("FFFFAFFFFBFFFFAFFFFBFFFFAFFFFBFF", rules, probs, 4, 35).setLeafType(BlockManager.getInstance().getBlock("engine:DarkLeaf")).setGenerationProbability(0.02f).setBarkType(BlockManager.getInstance().getBlock("engine:BirkTrunk"));

        // Oak variation tree
        rules = ImmutableMap.<String, String>builder()
            .put("A", "[&FFBFA]////[&BFFFA]////[&FBFFAFFA]")
            .put("B", "[&FFFAFFFF]////[&FFFAFFF]////[&FFFAFFAA]").build();
        TreeGenerator oakVariationTree = new TreeGeneratorLSystem("FFFFFFA", rules, probs, 4, 35).setGenerationProbability(0.08f);

        // A red tree
        rules = ImmutableMap.<String, String>builder()
            .put("A", "[&FFAFF]////[&FFAFF]////[&FFAFF]").build();
        TreeGenerator redTree = new TreeGeneratorLSystem("FFFFFAFAFAF", rules, probs, 4, 40).setLeafType(BlockManager.getInstance().getBlock("engine:RedLeaf")).setGenerationProbability(0.05f);

        // Cactus
        TreeGenerator cactus = new TreeGeneratorCactus().setGenerationProbability(0.05f);

        // Add the trees to the generator lists
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.MOUNTAINS, oakTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.MOUNTAINS, pineTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.FOREST, oakVariationTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.FOREST, oakTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.FOREST, pineTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.FOREST, oakVariationTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.SNOW, birkTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.PLAINS, redTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.PLAINS, oakTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.DESERT, cactus);
    }
}