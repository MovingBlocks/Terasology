import com.github.begla.blockmania.logic.generators.ChunkGeneratorTerrain.BIOME_TYPE
import com.github.begla.blockmania.logic.generators.TreeGeneratorCactus
import com.github.begla.blockmania.logic.generators.TreeGeneratorLSystem
import com.github.begla.blockmania.model.blocks.BlockManager

def mngr = generatorManager
def probs = ["A": 1.0d, "B": 0.8d]

// Default Oak
def rules = ["A": "[&FFBFA]////[&BFFFA]////[&FBFFA]", "B": "[&FFFA]////[&FFFA]////[&FFFA]"]
def oakTree = new TreeGeneratorLSystem(mngr, "FFFFFFA", rules, probs, 4, 30).withGenerationProbability(0.08)

// Pine
rules = ["A": "[&FFFBFFA]////[&FFFFBFA]////[&FFFBFFA]"]
def pineTree = new TreeGeneratorLSystem(mngr, "FFFFAFFFFFFFAFFFFA", rules, probs, 4, 20).withLeafType(BlockManager.getInstance().getBlock("DarkLeaf").getId()).withGenerationProbability(0.05).withBarkType(BlockManager.getInstance().getBlock("PineTrunk").getId())

// Birk
rules = ["A": "[&FFFAFFF]////[&FFAFFF]////[&FFFAFFF]", "B": "[&FBF]////[&FBF]////[&FBF]"]
def birkTree = new TreeGeneratorLSystem(mngr, "FFFFAFFFFBFFFFAFFFFBFFFFAFFFFBFF", rules, probs, 4, 30).withLeafType(BlockManager.getInstance().getBlock("DarkLeaf").getId()).withGenerationProbability(0.02).withBarkType(BlockManager.getInstance().getBlock("BirkTrunk").getId())

// Oak variation tree
rules = ["A": "[&FFBFA]////[&BFFFA]////[&FBFFAFFA]", "B": "[&FFFAFFFF]////[&FFFAFFF]////[&FFFAFFAA]"]
def oakVariationTree = new TreeGeneratorLSystem(mngr, "FFFFFFA", rules, probs, 4, 20).withGenerationProbability(0.08)

// A red tree
rules = ["A": "[&BFA]////[&FFBA]////[&FFBFA]"]
def redTree = new TreeGeneratorLSystem(mngr, "FFAFAFFAFF", rules, probs, 4, 30).withLeafType(BlockManager.getInstance().getBlock("RedLeaf").getId()).withGenerationProbability(0.05)

// Cactus
def cactus = new TreeGeneratorCactus(mngr).withGenerationProbability(0.05)

// Add the trees to the generator lists
mngr.addTreeGenerator BIOME_TYPE.MOUNTAINS, oakTree
mngr.addTreeGenerator BIOME_TYPE.MOUNTAINS, pineTree
mngr.addTreeGenerator BIOME_TYPE.MOUNTAINS, redTree

mngr.addTreeGenerator BIOME_TYPE.FOREST, oakTree
mngr.addTreeGenerator BIOME_TYPE.FOREST, pineTree
mngr.addTreeGenerator BIOME_TYPE.FOREST, redTree
mngr.addTreeGenerator BIOME_TYPE.FOREST, oakVariationTree

mngr.addTreeGenerator BIOME_TYPE.SNOW, birkTree

mngr.addTreeGenerator BIOME_TYPE.PLAINS, redTree
mngr.addTreeGenerator BIOME_TYPE.PLAINS, cactus

mngr.addTreeGenerator BIOME_TYPE.DESERT, redTree
mngr.addTreeGenerator BIOME_TYPE.DESERT, cactus
