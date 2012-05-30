// Just cheating - temp internal version of the file in the external groovy dir
// TODO: Find a nice generic way of embedding the tree rules into the relevant block definition for said tree

package org.terasology.logic.generators

import org.terasology.logic.generators.ChunkGeneratorTerrain.BIOME_TYPE
import org.terasology.model.blocks.management.BlockManager
import org.terasology.logic.world.generator.TreeGeneratorLSystem
import org.terasology.logic.world.generator.TreeGeneratorCactus
import org.terasology.logic.world.generator.ForestGenerator

import org.terasology.logic.world.WorldBiomeProvider

public class DefaultGenerators {

    public DefaultGenerators(ForestGenerator mngr) {

        def probs = ["A": 1.0d, "B": 0.8d]

        // Default Oak
        def rules = ["A": "[&FFBFA]////[&BFFFA]////[&FBFFA]", "B": "[&FFFA]////[&FFFA]////[&FFFA]"]
        def oakTree = new TreeGeneratorLSystem("FFFFFFA", rules, probs, 4, 30).setGenerationProbability(0.08)

        // Pine
        rules = ["A": "[&FFFFFA]////[&FFFFFA]////[&FFFFFA]"]
        def pineTree = new TreeGeneratorLSystem("FFFFAFFFFFFFAFFFFA", rules, probs, 4, 35).setLeafType(BlockManager.getInstance().getBlock("DarkLeaf")).setGenerationProbability(0.05).setBarkType(BlockManager.getInstance().getBlock("PineTrunk"))

        // Birk
        rules = ["A": "[&FFFAFFF]////[&FFAFFF]////[&FFFAFFF]", "B": "[&FAF]////[&FAF]////[&FAF]"]
        def birkTree = new TreeGeneratorLSystem("FFFFAFFFFBFFFFAFFFFBFFFFAFFFFBFF", rules, probs, 4, 35).setLeafType(BlockManager.getInstance().getBlock("DarkLeaf")).setGenerationProbability(0.02).setBarkType(BlockManager.getInstance().getBlock("BirkTrunk"))

        // Oak variation tree
        rules = ["A": "[&FFBFA]////[&BFFFA]////[&FBFFAFFA]", "B": "[&FFFAFFFF]////[&FFFAFFF]////[&FFFAFFAA]"]
        def oakVariationTree = new TreeGeneratorLSystem("FFFFFFA", rules, probs, 4, 35).setGenerationProbability(0.08)

        // A red tree
        rules = ["A": "[&FFAFF]////[&FFAFF]////[&FFAFF]"]
        def redTree = new TreeGeneratorLSystem("FFFFFAFAFAF", rules, probs, 4, 40).setLeafType(BlockManager.getInstance().getBlock("RedLeaf")).setGenerationProbability(0.05)

        // Cactus
        def cactus = new TreeGeneratorCactus().setGenerationProbability(0.05)

        // Add the trees to the generator lists
        mngr.addTreeGenerator WorldBiomeProvider.Biome.MOUNTAINS, oakTree
        mngr.addTreeGenerator WorldBiomeProvider.Biome.MOUNTAINS, pineTree
        mngr.addTreeGenerator WorldBiomeProvider.Biome.MOUNTAINS, redTree

        mngr.addTreeGenerator WorldBiomeProvider.Biome.FOREST, oakTree
        mngr.addTreeGenerator WorldBiomeProvider.Biome.FOREST, pineTree
        mngr.addTreeGenerator WorldBiomeProvider.Biome.FOREST, redTree
        mngr.addTreeGenerator WorldBiomeProvider.Biome.FOREST, oakVariationTree

        mngr.addTreeGenerator WorldBiomeProvider.Biome.SNOW, birkTree

        mngr.addTreeGenerator WorldBiomeProvider.Biome.PLAINS, redTree
        mngr.addTreeGenerator WorldBiomeProvider.Biome.PLAINS, cactus

        mngr.addTreeGenerator WorldBiomeProvider.Biome.DESERT, redTree
        mngr.addTreeGenerator WorldBiomeProvider.Biome.DESERT, cactus
    }
}