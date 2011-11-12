import com.github.begla.blockmania.generators.TreeGeneratorLSystem
import com.github.begla.blockmania.blocks.BlockManager
import com.github.begla.blockmania.generators.GeneratorManager

def GeneratorManager m = generatorManager;

// Create a bushy L-System based tree
HashMap<String, String> rules = new HashMap<String, String>();
rules.put("A", "[&FFFA]////[&FFFA]////[&FFFA]");

TreeGeneratorLSystem t1 = new TreeGeneratorLSystem(m, "FFFFFFA", rules).setGenProbability(1.0);

// ...and LARGE bushy tree
rules = new HashMap<String, String>();
rules.put("A", "[&FFFFFA]////[&FFFFFA]////[&FFFFFA]");

TreeGeneratorLSystem t2 = new TreeGeneratorLSystem(m, "FFFFAFFFFFFFAFFFFA", rules).setLeafType(BlockManager.getInstance().getBlock("Dark leaf").getId()).setGenProbability(0.1);

// ...and some strange wobbly thingy
rules = new HashMap<String, String>();
rules.put("A", "[&FA]////[&FFA]////[&FFFA]");

TreeGeneratorLSystem t3 = new TreeGeneratorLSystem(m, "FFAFAFFAFF", rules).setLeafType(BlockManager.getInstance().getBlock("Red leaf").getId()).setGenProbability(0.3);

// Add the trees to the generator list
m.getTreeGenerators().add(t3);
m.getTreeGenerators().add(t2);
m.getTreeGenerators().add(t1);