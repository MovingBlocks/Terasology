package org.terasology.logic.grammar;

import org.junit.Before;
import org.junit.Test;
import org.terasology.logic.grammar.assets.Grammar;
import org.terasology.logic.grammar.shapes.Shape;
import org.terasology.logic.grammar.shapes.ShapeSymbol;
import org.terasology.logic.grammar.shapes.complex.*;
import org.terasology.math.Vector3i;
import org.terasology.model.structures.BlockCollection;
import org.terasology.model.structures.BlockPosition;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.generator.building.BuildingGenerator;

import java.util.*;

import static junit.framework.Assert.assertEquals;

/** Created with IntelliJ IDEA. User: tobias Date: 28.08.12 Time: 19:07 To change this template use File | Settings | File Templates. */
public class BuildingGeneratorTest {
    private static Vector3i size = new Vector3i(4, 6, 6);

    static {
        BlockManager.getInstance().addBlockFamily(new SymmetricFamily(new BlockUri("engine:cobblestone"), new Block()));
        BlockManager.getInstance().addBlockFamily(new SymmetricFamily(new BlockUri("engine:plank"), new Block()));
    }

    private BuildingGenerator generator;

    private BlockCollection expectedBuilding;

    @Before
    public void setUp() throws Exception {
        // symbols used in this test
        ShapeSymbol axiom = new ShapeSymbol("axiom");
        ShapeSymbol house = new ShapeSymbol("house");
        ShapeSymbol floor = new ShapeSymbol("floor");
        ShapeSymbol roof = new ShapeSymbol("roof");
        // used Set rules
        SetRule setStone = new SetRule(new BlockUri("engine:cobblestone"));
        SetRule setPlank = new SetRule(new BlockUri("engine:plank"));
        // used split rule (split walls)
        SplitArg walls = new SplitArg(SplitArg.SplitType.WALLS, setStone);
        SplitRule splitWalls = new SplitRule(Arrays.asList(walls));
        // used divide rule
        DivideArg floorArg = new DivideArg(new Size(1f, false), floor);
        DivideArg roofArg = new DivideArg(new Size(1f, true), roof);
        List<DivideArg> divArgs = new ArrayList<DivideArg>(2);
        divArgs.add(floorArg);
        divArgs.add(roofArg);
        DivideRule divHouse = new DivideRule(divArgs, DivideRule.Direction.Y);

        // fill the map with rules
        Map<String, List<Shape>> rules = new HashMap<String, List<Shape>>();
        rules.put(axiom.getLabel(), shapeToList(house));
        rules.put(house.getLabel(), shapeToList(divHouse));
        rules.put(floor.getLabel(), shapeToList(splitWalls));
        rules.put(roof.getLabel(), shapeToList(setPlank));

        ProductionSystem system = new ProductionSystem(rules, axiom);
        Grammar grammar = new Grammar(system);


        generator = new BuildingGenerator(grammar);

        // build up block collection for expected building
        expectedBuilding = new BlockCollection();
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                for (int z = 0; z < size.z; z++) {
                    if (y == size.y - 1) {
                        expectedBuilding.addBlock(new BlockPosition(x, y, -z), BlockManager.getInstance().getBlock
                                ("engine:plank"));
                    } else if (x == 0 || x == size.x - 1 || z == 0 || z == size.z - 1) {
                        expectedBuilding.addBlock(new BlockPosition(x, y, -z), BlockManager.getInstance().getBlock
                                ("engine:cobblestone"));
                    }
                }
            }
        }

    }

    private List<Shape> shapeToList(Shape symbol) {
        List<Shape> l = new ArrayList<Shape>(1);
        l.add(symbol);
        return l;
    }

    @Test
    public void testBuildingGenerator() throws Exception {
        BlockCollection resultingBuilding = generator.generate(size.x, size.y, size.z);

        assertEquals(expectedBuilding.getBlocks().size(), resultingBuilding.getBlocks().size());
        assertEquals(expectedBuilding, resultingBuilding);
    }
}