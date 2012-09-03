package org.terasology.world.generator.building;

import org.terasology.logic.grammar.*;
import org.terasology.logic.grammar.assets.Grammar;
import org.terasology.world.block.BlockUri;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: tobias
 * Date: 28.08.12
 * Time: 19:07
 * To change this template use File | Settings | File Templates.
 */
public class BuildingGeneratorTest {

    ShapeSymbol axiom, house, floor, roof;
    Map<ShapeSymbol, List<Shape>> rules;

    public static void main(String[] args) {
        new BuildingGeneratorTest();
    }

    public BuildingGeneratorTest() {
        // symbols used in this test
        ShapeSymbol axiom = new ShapeSymbol("axiom");
        ShapeSymbol house = new ShapeSymbol("house");
        ShapeSymbol floor = new ShapeSymbol("floor");
        ShapeSymbol roof = new ShapeSymbol("roof");
        // used Set rules
        SetRule setStone = new SetRule(new BlockUri("engine:cobblestone"));
        SetRule setPlank = new SetRule(new BlockUri("engine:plank"));
        // used split rule
        SplitArg walls = new SplitArg(SplitArg.SplitType.WALLS, setStone);
        SplitRule splitWalls = new SplitRule(Arrays.asList(walls));
        // used divide rule
        DivideArg floorArg = new DivideArg(new Size(3f, true), floor);
        DivideArg roofArg = new DivideArg(new Size(1f, true), roof);
        List<DivideArg> divArgs = new ArrayList<DivideArg>(2);
        divArgs.add(floorArg);
        divArgs.add(roofArg);
        DivideRule divHouse = new DivideRule(divArgs, DivideRule.Direction.Y);

        // fill the map with rules
        rules = new HashMap<ShapeSymbol, List<Shape>>();
        rules.put(axiom, shapeToList(house));
        rules.put(house, shapeToList(divHouse));
        rules.put(floor, shapeToList(splitWalls));
        rules.put(roof, shapeToList(setPlank));

        ProductionSystem system = new ProductionSystem(rules, axiom);
        Grammar grammar = new Grammar(system);

        BuildingGenerator generator = new BuildingGenerator(grammar);

        // start the generation
        generator.generate(4, 6, 5);
    }

    public List<Shape> shapeToList(Shape symbol) {
        List<Shape> l = new ArrayList<Shape>(1);
        l.add(symbol);
        return l;
    }
}
