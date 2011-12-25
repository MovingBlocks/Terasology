import com.github.begla.blockmania.logic.generators.ChunkGeneratorTerrain.BIOME_TYPE
import com.github.begla.blockmania.logic.generators.GeneratorManager
import com.github.begla.blockmania.logic.generators.TreeGeneratorCactus
import com.github.begla.blockmania.logic.generators.TreeGeneratorLSystem
import com.github.begla.blockmania.model.blocks.BlockManager

def GeneratorManager m = generatorManager;

HashMap<String, Double> probabilities = new HashMap<String, Double>();
probabilities.put("A", 1.0d);
probabilities.put("B", 0.4d);

// Create a bushy L-System based tree
HashMap<String, String> rules = new HashMap<String, String>();
rules.put("A", "[&FFBFA]////[&BFFFA]////[&FBFFA]");
rules.put("B", "[&FFFA]////[&FFFA]////[&FFFA]");

def t1 = new TreeGeneratorLSystem(m, "FFFFFFA", rules, probabilities, 5, 30).withGenerationProbability(0.15);

// Create a bushy L-System based tree
rules = new HashMap<String, String>();
rules.put("A", "[&FFBFA]////[&BFFFA]////[&FBFFAFFA]");
rules.put("B", "[&FFFAFFFF]////[&FFFAFFF]////[&FFFAFFAA]");

def t5 = new TreeGeneratorLSystem(m, "FFFFFFA", rules, probabilities, 5, 20).withGenerationProbability(0.05);

rules = new HashMap<String, String>();
rules.put("A", "[&FFBFA]////[&FBFFA]////[&FFBFA]");

def t4 = new TreeGeneratorLSystem(m, "FFFFFFFFFFFAFFFFFAFFFFFA", rules, probabilities, 5, 40).withLeafType(BlockManager.getInstance().getBlock("DarkLeaf").getId()).withGenerationProbability(0.05);

// ...and a LARGE bushy tree
rules = new HashMap<String, String>();
rules.put("A", "[&FFFBFFA]////[&FFFFBFA]////[&FFFBFFA]");

def t2 = new TreeGeneratorLSystem(m, "FFFFAFFFFFFFAFFFFA", rules, probabilities, 5, 20).withLeafType(BlockManager.getInstance().getBlock("DarkLeaf").getId()).withGenerationProbability(0.05);

// ...and some strange wobbly thingies
rules = new HashMap<String, String>();
rules.put("A", "[&BFA]////[&FFBA]////[&FFBFA]");

def t3 = new TreeGeneratorLSystem(m, "FFAFAFFAFF", rules, probabilities, 5, 30).withLeafType(BlockManager.getInstance().getBlock("RedLeaf").getId()).withGenerationProbability(0.08);

def c1 = new TreeGeneratorCactus(m).withGenerationProbability(0.05);

// Add the trees to the generator lists
m.addTreeGenerator BIOME_TYPE.FOREST, t1
m.addTreeGenerator BIOME_TYPE.MOUNTAINS, t1
m.addTreeGenerator BIOME_TYPE.FOREST, t2
m.addTreeGenerator BIOME_TYPE.MOUNTAINS, t2
m.addTreeGenerator BIOME_TYPE.FOREST, t3
m.addTreeGenerator BIOME_TYPE.MOUNTAINS, t3
m.addTreeGenerator BIOME_TYPE.FOREST, t5
m.addTreeGenerator BIOME_TYPE.MOUNTAINS, t5

m.addTreeGenerator BIOME_TYPE.SNOW, t4

m.addTreeGenerator BIOME_TYPE.PLAINS, t3
m.addTreeGenerator BIOME_TYPE.DESERT, t3
m.addTreeGenerator BIOME_TYPE.PLAINS, c1
m.addTreeGenerator BIOME_TYPE.DESERT, c1