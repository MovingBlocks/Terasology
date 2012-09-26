/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.generator.building;

import org.terasology.logic.grammar.*;
import org.terasology.logic.grammar.assets.Grammar;
import org.terasology.model.structures.BlockCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author skaldarnar
 */
public class BuildingGenerator {

    private Grammar grammar;
    private ProductionSystem system;
    private Random rand;
    private Map<ShapeSymbol, List<Shape>> ruleMap;


    public BuildingGenerator(Grammar grammar) {
        this.grammar = grammar;
        rand = new Random();

        system = grammar.getProductionSystem();
        ruleMap = system.getRules();
    }

    public BlockCollection generate(int width, int height, int depth) {
        System.out.println("Starting generation ...");

        // building up a derivation tree
        // root of the tree is the initial axiom
        Shape axiom = system.getInitialAxiom();
        axiom.setDimension(width, height, depth);
        Tree root = new TreeNode(axiom);

        List<TreeNode> activeNodes = new ArrayList<TreeNode>();
        activeNodes.add((TreeNode) root);

        while (!activeNodes.isEmpty()) {
            for (TreeNode t : activeNodes) {
                List<Shape> successors = new ArrayList<Shape>();
                Shape s = t.getShape();
                System.out.println("Active shape: \n" + s.toString());
                // if _t_ contains a ShapeSymbol, select a rule from the production system
                if (s instanceof ShapeSymbol) {
                    Shape selectedRule = selectRule((ShapeSymbol) s);
                    selectedRule.setDimension(s.getDimension());
                    selectedRule.setPosition(s.getPosition());
                    selectedRule.setCoordinateSystem(s.getCoordinateSystem());
                    successors.add(selectedRule);
                } else if (s instanceof ComplexRule) {
                    ComplexRule c = (ComplexRule) s;
                    successors = c.getElements();
                }
                t.setActive(false);
                for (Shape succ : successors) {
                    if (succ instanceof TerminalShape) {
                        t.add(new TreeLeaf((TerminalShape) succ));
                    } else {
                        t.add(new TreeNode(succ));
                    }
                }
            }
            // find all active nodes using a depth first search
            // TODO: use a stack or map for active nodes
            activeNodes = root.findActiveNodes();
        }

        // traverse the tree to construct the actual BlockCollection
        BlockCollection building = root.derive();
        // return the constructed blueprint
        return building;
    }

    private Shape selectRule(ShapeSymbol symbol) {
        List<Shape> successorShapes = ruleMap.get(symbol);
        if (successorShapes == null) {
            return null;
        }
        int index = rand.nextInt(successorShapes.size());
        return successorShapes.get(index);
    }
}
