import com.github.begla.blockmania.blocks.BlockManager
import com.github.begla.blockmania.generators.ChunkGeneratorTerrain.BIOME_TYPE
import com.github.begla.blockmania.generators.GeneratorManager
import com.github.begla.blockmania.generators.TreeGeneratorCactus
import com.github.begla.blockmania.generators.TreeGeneratorLSystem

def GeneratorManager m = generatorManager;

HashMap<String, String> probabilities = new HashMap<String, Double>();
probabilities.put("A", 0.7d);
probabilities.put("B", 0.6d);

// Create a bushy L-System based tree
HashMap<String, String> rules = new HashMap<String, String>();
rules.put("A", "[&FFBFA]////[&BFFFA]////[&FBFFA]");
rules.put("B", "[&FFFA]////[&FFFA]////[&FFFA]");

def t1 = new TreeGeneratorLSystem(m, "FFFFFFA", rules, probabilities, 6, 30).withGenerationProbability(0.2);

rules = new HashMap<String, String>();
rules.put("A", "[&FFBFA]////[&FBFFA]////[&FFBFA]");

def t4 = new TreeGeneratorLSystem(m, "FFFFFFFFFFFAFFFFFAFFFFFA", rules, probabilities, 6, 40).withLeafType(BlockManager.getInstance().getBlock("Dark leaf").getId()).withGenerationProbability(0.01);

// ...and a LARGE bushy tree
rules = new HashMap<String, String>();
rules.put("A", "[&FFFBFFA]////[&FFFFBFA]////[&FFFBFFA]");

def t2 = new TreeGeneratorLSystem(m, "FFFFAFFFFFFFAFFFFA", rules, probabilities, 6, 20).withLeafType(BlockManager.getInstance().getBlock("Dark leaf").getId()).withGenerationProbability(0.01);

// ...and some strange wobbly thingies
rules = new HashMap<String, String>();
rules.put("A", "[&BFA]////[&FFBA]////[&FFBFA]");

def t3 = new TreeGeneratorLSystem(m, "FFAFAFFAFF", rules, probabilities, 6, 30).withLeafType(BlockManager.getInstance().getBlock("Red leaf").getId()).withGenerationProbability(0.05);

def c1 = new TreeGeneratorCactus(m).withGenerationProbability(0.05);

// Add the trees to the generator lists
m.addTreeGenerator BIOME_TYPE.FOREST, t1
m.addTreeGenerator BIOME_TYPE.MOUNTAINS, t1
m.addTreeGenerator BIOME_TYPE.FOREST, t2
m.addTreeGenerator BIOME_TYPE.MOUNTAINS, t2

m.addTreeGenerator BIOME_TYPE.SNOW, t4

m.addTreeGenerator BIOME_TYPE.PLAINS, t3
m.addTreeGenerator BIOME_TYPE.DESERT, t3
m.addTreeGenerator BIOME_TYPE.PLAINS, c1
m.addTreeGenerator BIOME_TYPE.DESERT, c1