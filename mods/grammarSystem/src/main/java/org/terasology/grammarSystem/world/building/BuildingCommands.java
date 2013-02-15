/*
* Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.terasology.grammarSystem.world.building;

import org.terasology.game.CoreRegistry;
import org.terasology.grammarSystem.logic.grammar.ProductionSystem;
import org.terasology.grammarSystem.logic.grammar.assets.Grammar;
import org.terasology.grammarSystem.logic.grammar.shapes.Shape;
import org.terasology.grammarSystem.logic.grammar.shapes.ShapeSymbol;
import org.terasology.grammarSystem.logic.grammar.shapes.complex.*;
import org.terasology.logic.commands.Command;
import org.terasology.logic.commands.CommandParam;
import org.terasology.logic.commands.CommandProvider;
import org.terasology.logic.commands.Commands;
import org.terasology.logic.manager.MessageManager;
import org.terasology.model.structures.BlockCollection;
import org.terasology.model.structures.BlockPosition;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Vector3f;
import java.util.*;

/** @author skaldarnar */
public class BuildingCommands implements CommandProvider {

    @Command(shortDescription = "Building generation test")
    public void build() {
        MessageManager.getInstance().addMessage("Starting building a structure ...");

        BuildingGenerator generator = setUp();
        BlockCollection collection = generator.generate(3, 12, 3);

        collection.setAttachPos(new BlockPosition(0, 0, 0));

        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        Vector3f attachPos = camera.getPosition();
        Vector3f offset = camera.getViewingDirection();
        offset.scale(3);
        attachPos.add(offset);

        WorldProvider provider = CoreRegistry.get(WorldProvider.class);
        if (provider != null) {
            collection.build(provider, new BlockPosition(attachPos));
        }

        MessageManager.getInstance().addMessage("Finished ...");
    }

    @Command(shortDescription = "Place a building with specified size in front of the player",
            helpText = "Places a constructed building in front of the player. For construction, " +
                    "a predefined org.terasology.logic.grammar is used. The bounding box of the generated structure is given by the " +
                    "specified arguments.")
    public void build(@CommandParam(name = "width") int width, @CommandParam(name = "height") int height,
                      @CommandParam(name = "depth") int depth) {
        StringBuilder builder = new StringBuilder();
        builder.append("Starting building generation with predefined org.terasology.logic.grammar, using the given dimensions of ");
        builder.append("width = ").append(width).append(", ");
        builder.append("height = ").append(height).append(", ");
        builder.append("depth = ").append(depth).append(".");

        MessageManager.getInstance().addMessage(builder.toString());

        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        Vector3f attachPos = camera.getPosition();
        Vector3f offset = camera.getViewingDirection();

        offset.scale(3);
        attachPos.add(offset);

        //BuildingGenerator generator = setUp();
        BuildingGenerator generator = complexBuildingGenerator();
        //BuildingGenerator generator = testGenerator();

        MessageManager.getInstance().addMessage("Created Building Generator. Starting building process ...");
        long time = System.currentTimeMillis();

        BlockCollection collection = generator.generate(width, height, depth);

        time = System.currentTimeMillis() - time;
        builder = new StringBuilder("Created collection in ");
        builder.append(time).append(" ms");
        MessageManager.getInstance().addMessage(builder.toString());

        collection.setAttachPos(new BlockPosition(0, 0, 0));

        WorldProvider provider = CoreRegistry.get(WorldProvider.class);
        if (provider != null) {
            collection.build(provider, new BlockPosition(attachPos));
        }

        MessageManager.getInstance().addMessage("Finished ...");
    }

    public BuildingGenerator setUp() {
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
        rules.put(axiom.getLabel(), Arrays.<Shape>asList(house));
        rules.put(house.getLabel(), Arrays.<Shape>asList(divHouse));
        rules.put(floor.getLabel(), Arrays.<Shape>asList(splitWalls));
        rules.put(roof.getLabel(), Arrays.<Shape>asList(setPlank));

        ProductionSystem system = new ProductionSystem(rules, axiom);
        Grammar grammar = new Grammar(system);


        return new BuildingGenerator(grammar);
    }

    public static BuildingGenerator complexBuildingGenerator() {
        //================================================
        ShapeSymbol house = new ShapeSymbol("house");
        ShapeSymbol ground_floor = new ShapeSymbol("ground_floor");
        ShapeSymbol wall = new ShapeSymbol("wall");
        ShapeSymbol wall_inner = new ShapeSymbol("wall_inner");
        ShapeSymbol win_wall = new ShapeSymbol("win_wall");
        ShapeSymbol door = new ShapeSymbol("door");
        ShapeSymbol border = new ShapeSymbol("border");
        ShapeSymbol middle_part = new ShapeSymbol("middle_part");
        ShapeSymbol middle_part_walls = new ShapeSymbol("middle_part_walls");
        //================================================
        SetRule setStone = new SetRule(BlockManager.getInstance().resolveBlockUri("stone").get(0));
        SetRule setCobblestone = new SetRule(BlockManager.getInstance().resolveBlockUri("cobblestone").get(0));
        SetRule setOaktrunk = new SetRule(BlockManager.getInstance().resolveBlockUri("oaktrunk").get(0));
        SetRule setGlass = new SetRule(BlockManager.getInstance().resolveBlockUri("glass").get(0));
        SetRule setPlank = new SetRule(BlockManager.getInstance().resolveBlockUri("plank").get(0));
        SetRule setAir = new SetRule(BlockManager.getInstance().resolveBlockUri("air").get(0));
        //================================================
        DivideArg divideHouseGroundFloorArg = new DivideArg(new Size(3f, true), ground_floor);
        DivideArg divideHouseBorderArg = new DivideArg(new Size(1f, true), border);
        DivideArg divideHouseMiddlePartArg = new DivideArg(new Size(1f, false), middle_part);
        DivideArg divideHouseRoofArg = new DivideArg(new Size(1f, true), setPlank.clone());
        LinkedList<DivideArg> divideHouseArgs = new LinkedList<DivideArg>();
        divideHouseArgs.add(divideHouseGroundFloorArg);
        divideHouseArgs.add(divideHouseBorderArg);
        divideHouseArgs.add(divideHouseMiddlePartArg);
        divideHouseArgs.add(divideHouseRoofArg);
        DivideRule divideHouse = new DivideRule(divideHouseArgs, DivideRule.Direction.Y);

        DivideArg divideWallStone1Arg = new DivideArg(new Size(1f, true), setStone.clone());
        DivideArg divideWallInnerArg = new DivideArg(new Size(1f, false), wall_inner);
        DivideArg divideWallStone2Arg = new DivideArg(new Size(1f, true), setStone.clone());
        LinkedList<DivideArg> divideWallArgs = new LinkedList<DivideArg>();
        divideWallArgs.add(divideWallStone1Arg);
        divideWallArgs.add(divideWallInnerArg);
        divideWallArgs.add(divideWallStone2Arg);
        DivideRule divideWall = new DivideRule(divideWallArgs, DivideRule.Direction.X);

        DivideArg divideWallInnerCobblestoneArg = new DivideArg(new Size(.3f, false), setCobblestone.clone());
        DivideArg divideWallInnerDoorArg = new DivideArg(new Size(1f, true), door);
        DivideArg divideWallInnerWinWallArg = new DivideArg(new Size(.7f, false), win_wall);
        LinkedList<DivideArg> divideWallInnerArgs = new LinkedList<DivideArg>();
        divideWallInnerArgs.add(divideWallInnerCobblestoneArg);
        divideWallInnerArgs.add(divideWallInnerDoorArg);
        divideWallInnerArgs.add(divideWallInnerWinWallArg);
        DivideRule divideWallInner = new DivideRule(divideWallInnerArgs, DivideRule.Direction.X);

        DivideArg divideWinWallBottomArg = new DivideArg(new Size(1f, true), setCobblestone.clone());
        DivideArg divideWinWallMiddleArg = new DivideArg(new Size(1f, true), setGlass.clone());
        DivideArg divideWinWallTopArg = new DivideArg(new Size(1f, true), setCobblestone.clone());
        LinkedList<DivideArg> divideWinWallArgs = new LinkedList<DivideArg>();
        divideWinWallArgs.add(divideWinWallBottomArg);
        divideWinWallArgs.add(divideWinWallMiddleArg);
        divideWinWallArgs.add(divideWinWallTopArg);
        DivideRule divideWinWall = new DivideRule(divideWinWallArgs, DivideRule.Direction.Y);

        DivideArg doorAirArg = new DivideArg(new Size(2f, true), setAir);
        DivideArg doorTopArg = new DivideArg(new Size(1f, true), setCobblestone);
        LinkedList<DivideArg> divideDoorArgs = new LinkedList<DivideArg>();
        divideDoorArgs.add(doorAirArg);
        divideDoorArgs.add(doorTopArg);
        DivideRule divideDoor = new DivideRule(divideDoorArgs, DivideRule.Direction.Y);

        DivideArg divideMiddlePartWallsLeft = new DivideArg(new Size(1f, true), setOaktrunk.clone());
        DivideArg divideMiddlePartWallsCenter = new DivideArg(new Size(1f, false), setStone.clone());
        DivideArg divideMiddlePartWallsRight = new DivideArg(new Size(1f, true), setOaktrunk.clone());
        LinkedList<DivideArg> divideMiddlePartWallsArgs = new LinkedList<DivideArg>();
        divideMiddlePartWallsArgs.add(divideMiddlePartWallsLeft);
        divideMiddlePartWallsArgs.add(divideMiddlePartWallsCenter);
        divideMiddlePartWallsArgs.add(divideMiddlePartWallsRight);
        DivideRule divideMiddlePartWalls = new DivideRule(divideMiddlePartWallsArgs, DivideRule.Direction.X);
        //================================================
        SplitArg splitWallArg = new SplitArg(SplitArg.SplitType.WALLS, wall);
        SplitRule splitGroundFloor = new SplitRule(Arrays.asList(splitWallArg));

        SplitArg splitBorderArg = new SplitArg(SplitArg.SplitType.WALLS, setOaktrunk);
        SplitRule splitBorder = new SplitRule(Arrays.asList(splitBorderArg));

        //SplitArg splitMiddlePartArg = new SplitArg(SplitArg.SplitType.WALLS, divideMiddlePartWalls);
        //SplitArg splitMiddlePartArg = new SplitArg(SplitArg.SplitType.WALLS, setCobblestone.clone());
        SplitArg splitMiddlePartArg = new SplitArg(SplitArg.SplitType.WALLS, middle_part_walls);
        SplitRule splitMiddlePart = new SplitRule(Arrays.asList(splitMiddlePartArg));
        //================================================
        Map<String, List<Shape>> rules = new HashMap<String, List<Shape>>();
        rules.put(house.getLabel(), Arrays.<Shape>asList(divideHouse));
        rules.put(ground_floor.getLabel(), Arrays.<Shape>asList(splitGroundFloor));
        rules.put(wall.getLabel(), Arrays.<Shape>asList(divideWall));
        rules.put(wall_inner.getLabel(), Arrays.<Shape>asList(divideWallInner));
        rules.put(win_wall.getLabel(), Arrays.<Shape>asList(divideWinWall));
        rules.put(door.getLabel(), Arrays.<Shape>asList(divideDoor));
        rules.put(border.getLabel(), Arrays.<Shape>asList(splitBorder));
        rules.put(middle_part.getLabel(), Arrays.<Shape>asList(splitMiddlePart));
        rules.put(middle_part_walls.getLabel(), Arrays.<Shape>asList(divideMiddlePartWalls));

        ProductionSystem system = new ProductionSystem(rules, house);
        Grammar grammar = new Grammar(system);

        return new BuildingGenerator(grammar);
    }

    public static BuildingGenerator testGenerator() {
        Map<String, List<Shape>> rules = new HashMap<String, List<Shape>>();

        SetRule setStone = new SetRule(new BlockUri("engine:stone"));

        SetRule setGlass = new SetRule(new BlockUri("engine:glass"));

        DivideArg winWallStoneArg1 = new DivideArg(new Size(.5f, false), setStone);
        DivideArg winWallGlassArg = new DivideArg(new Size(1f, true), setGlass);
        DivideArg winWallStoneArg2 = new DivideArg(new Size(.5f, false), setStone);
        List<DivideArg> args = new ArrayList<DivideArg>();
        args.add(winWallStoneArg1);
        args.add(winWallGlassArg);
        args.add(winWallStoneArg2);
        DivideRule winWallDivide = new DivideRule(args, DivideRule.Direction.X);

        ShapeSymbol wall = new ShapeSymbol("wall");
        rules.put(wall.getLabel(), Arrays.<Shape>asList(winWallDivide));

        SplitArg splitWallsArgs = new SplitArg(SplitArg.SplitType.WALLS, wall);
        SplitRule splitWalls = new SplitRule(Arrays.asList(splitWallsArgs));

        ShapeSymbol floor = new ShapeSymbol("floor");
        rules.put(floor.getLabel(), Arrays.<Shape>asList(splitWalls));

        Grammar grammar = new Grammar(new ProductionSystem(rules, floor));
        return new BuildingGenerator(grammar);
    }
}