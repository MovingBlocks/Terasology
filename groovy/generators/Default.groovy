import com.github.begla.blockmania.blocks.BlockManager
import com.github.begla.blockmania.generators.ChunkGeneratorTerrain.BIOME_TYPE
import com.github.begla.blockmania.generators.GeneratorManager
import com.github.begla.blockmania.generators.TreeGeneratorCactus
import com.github.begla.blockmania.generators.TreeGeneratorLSystem

def GeneratorManager m = generatorManager;

// Create a bushy L-System based tree
HashMap<String, String> rules = new HashMap<String, String>();
rules.put("A", "[&FFFA]////[&FFFA]////[&FFFA]");

def t1 = new TreeGeneratorLSystem(m, "FFFFFFA", rules).withGenerationProbability(0.2);

rules = new HashMap<String, String>();
rules.put("A", "[&FFFA]////[&FFFA]////[&FFFA]");

def t4 = new TreeGeneratorLSystem(m, "FFFFFFFFFFFAFFFFFAFFFFFA", rules).withLeafType(BlockManager.getInstance().getBlock("Dark leaf").getId()).withGenerationProbability(0.01);

// ...and a LARGE bushy tree
rules = new HashMap<String, String>();
rules.put("A", "[&FFFFFA]////[&FFFFFA]////[&FFFFFA]");

def t2 = new TreeGeneratorLSystem(m, "FFFFAFFFFFFFAFFFFA", rules).withLeafType(BlockManager.getInstance().getBlock("Dark leaf").getId()).withGenerationProbability(0.01);

// ...and some strange wobbly thingies
rules = new HashMap<String, String>();
rules.put("A", "[&FA]////[&FFA]////[&FFFA]");

def t3 = new TreeGeneratorLSystem(m, "FFAFAFFAFF", rules).withLeafType(BlockManager.getInstance().getBlock("Red leaf").getId()).withGenerationProbability(0.05);

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