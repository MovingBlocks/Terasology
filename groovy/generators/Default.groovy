import com.github.begla.blockmania.logic.generators.ChunkGeneratorTerrain.BIOME_TYPE
import com.github.begla.blockmania.logic.generators.TreeGeneratorCactus
import com.github.begla.blockmania.logic.generators.TreeGeneratorLSystem
import com.github.begla.blockmania.model.blocks.BlockManager

def m = generatorManager

probabilities = ["A": 1.0d, "B": 0.8d]

// Default Oak
def rules = ["A": "[&FFBFA]////[&BFFFA]////[&FBFFA]", "B": "[&FFFA]////[&FFFA]////[&FFFA]"]
def t1 = new TreeGeneratorLSystem(m, "FFFFFFA", rules, probabilities, 4, 30).withGenerationProbability(0.08)

// Pine
rules = ["A": "[&FFFBFFA]////[&FFFFBFA]////[&FFFBFFA]"]
def t2 = new TreeGeneratorLSystem(m, "FFFFAFFFFFFFAFFFFA", rules, probabilities, 4, 20).withLeafType(BlockManager.getInstance().getBlock("DarkLeaf").getId()).withGenerationProbability(0.05).withBarkType(BlockManager.getInstance().getBlock("PineTrunk").getId())

// Birk
rules = ["A": "[&FFFAFFF]////[&FFAFFF]////[&FFFAFFF]", "B": "[&FBF]////[&FBF]////[&FBF]"]
def t4 = new TreeGeneratorLSystem(m, "FFFFAFFFFBFFFFAFFFFBFFFFAFFFFBFF", rules, probabilities, 4, 30).withLeafType(BlockManager.getInstance().getBlock("DarkLeaf").getId()).withGenerationProbability(0.02).withBarkType(BlockManager.getInstance().getBlock("BirkTrunk").getId())

// ???
rules = ["A": "[&FFBFA]////[&BFFFA]////[&FBFFAFFA]", "B": "[&FFFAFFFF]////[&FFFAFFF]////[&FFFAFFAA]"]
def t5 = new TreeGeneratorLSystem(m, "FFFFFFA", rules, probabilities, 4, 20).withGenerationProbability(0.08)

// ???
rules = ["A": "[&BFA]////[&FFBA]////[&FFBFA]"]
def t3 = new TreeGeneratorLSystem(m, "FFAFAFFAFF", rules, probabilities, 4, 30).withLeafType(BlockManager.getInstance().getBlock("RedLeaf").getId()).withGenerationProbability(0.05)

def c1 = new TreeGeneratorCactus(m).withGenerationProbability(0.05)

// Add the trees to the generator lists
m.addTreeGenerator BIOME_TYPE.MOUNTAINS, t1
m.addTreeGenerator BIOME_TYPE.MOUNTAINS, t2
m.addTreeGenerator BIOME_TYPE.MOUNTAINS, t3

m.addTreeGenerator BIOME_TYPE.FOREST, t1
m.addTreeGenerator BIOME_TYPE.FOREST, t2
m.addTreeGenerator BIOME_TYPE.FOREST, t3
m.addTreeGenerator BIOME_TYPE.FOREST, t5

m.addTreeGenerator BIOME_TYPE.SNOW, t4

m.addTreeGenerator BIOME_TYPE.PLAINS, t3
m.addTreeGenerator BIOME_TYPE.PLAINS, c1

m.addTreeGenerator BIOME_TYPE.DESERT, t3
m.addTreeGenerator BIOME_TYPE.DESERT, c1