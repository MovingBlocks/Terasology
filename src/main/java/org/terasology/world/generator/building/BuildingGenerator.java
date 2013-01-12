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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.grammar.ProductionSystem;
import org.terasology.logic.grammar.assets.Grammar;
import org.terasology.logic.grammar.shapes.Shape;
import org.terasology.logic.grammar.shapes.ShapeSymbol;
import org.terasology.logic.grammar.shapes.TerminalShape;
import org.terasology.logic.grammar.shapes.complex.ComplexRule;
import org.terasology.math.Matrix4i;
import org.terasology.model.structures.BlockCollection;

import java.util.*;

/**
 * @author Tobias 'skaldarnar' Nett
 *         <p/>
 *         A generator for building structures.
 *         <p/>
 *         The generator is instanciated with a specific {@link Grammar} which will be used to generate structures.
 *         <p/>
 *         The generator offers a public method {@code generate()}, which takes the dimension of the strucutre to build. The structure
 *         generated is returned as a {@code BlockCollection}.
 */
public class BuildingGenerator {

    // Logging with specific logging class
    private static final Logger logger = LoggerFactory.getLogger(BuildingGenerator.class);

    private Grammar grammar;                            // The grammar this generator is based on
    private ProductionSystem system;                    // The underlying production system
    private Map<String, List<Shape>> ruleMap;      // A map of simple rules

    private Random rand;                                // A random number generator - TODO: Replace with seed!

    /**
     * The BuildingGenerator has to be initialized with a {@code Grammar} as a basis for construction.
     *
     * @param grammar the grammar used for building generation
     */
    public BuildingGenerator(Grammar grammar) {
        this.grammar = grammar;
        rand = new Random();

        system = grammar.getProductionSystem();
        ruleMap = system.getRules();
    }

    public BlockCollection generate(int width, int height, int depth) {
        return generate(width, height, depth, Matrix4i.id());
    }

    /**
     * This method constructs a building, using the grammar specified for this generator and the dimensions passed to this method. Each
     * dimension must be greater zero (obviously). The generation process is fully random at this moment!
     *
     * @param width  the (maximal) width of the structure to generate
     * @param height the (maximal) height of the structure to generate
     * @param depth  the (maximal) depth of the structure to generate
     *
     * @return the generated building as {@code BlockCollection}
     */
    public BlockCollection generate(int width, int height, int depth, Matrix4i matrix) {

        logger.info("Starting structure generation with maximal dimension {}x{}x{} (width, height, depth)",
                width, height, depth);

        // Building up a derivation tree, using random derivations at the moment.
        // TODO: base derivation on world seed
        // The root of the tree is the initial axiom.
        Shape initialAxiom = system.getInitialAxiom();      // get the initial axiom (starting shape)
        initialAxiom.setDimension(width, height, depth);    // set the shape's dimensions accordingly to the arguments
        initialAxiom.setMatrix(matrix);                     // set the shapes orientation and translation
        Tree root = new TreeNode(initialAxiom);             // Place the initial axiom as the derivation tree's root

        // A list of _active_ nodes -- all nodes that are not derived.
        Queue<TreeNode> activeNodes = new LinkedList<TreeNode>();
        activeNodes.add((TreeNode) root);       // add the root as starting node to the list

        // This derivation loop runs until all active nodes are derived. That means after this loop we get the generated
        // structure by traversing the generated tree.
        while (!activeNodes.isEmpty()) {
            // Get the first active shape in the queue
            TreeNode t = activeNodes.poll();
            Shape s = t.getShape();
            logger.debug("Active shape: \t {}", s.toString());

            // Prepare a list for successor shapes
            List<Shape> successors = new ArrayList<Shape>();

            /**
             * If t contains a ShapeSymbol, select a rule from the production system. That means, selecting one of the
             * possible derivations.
             * Otherwise retrieve the successors from the ComplexShape. The successors of a complex shape are all
             * resulting sub rules of the complex rule.
             */
            if (s instanceof ShapeSymbol) {
                Shape selectedRule = selectRule((ShapeSymbol) s);
                selectedRule.setDimension(s.getDimension());
                //selectedRule.setPosition(s.getPosition());
                selectedRule.setMatrix(s.getMatrix());
                successors.add(selectedRule);
            } else if (s instanceof ComplexRule) {
                ComplexRule c = (ComplexRule) s;
                successors = c.getElements();
            }
            // Set the node t as inactive
            t.setActive(false);

            for (Shape succ : successors) {
                if (succ instanceof TerminalShape) {
                    t.add(new TreeLeaf((TerminalShape) succ));
                } else {
                    TreeNode tn = new TreeNode(succ);
                    t.add(tn);
                    activeNodes.offer(tn);
                }
            }
            logger.debug("Active Nodes: \t {}", activeNodes);
        }

        // traverse the tree to construct the actual BlockCollection
        BlockCollection building = root.derive();
        logger.info("Finished generation!");
        // return the constructed blueprint
        return building;
    }

    /**
     * Selects a rule for the given shape symbol from the possible rules. </p> The selection is fully random at the moment. All possible
     * rules are determined by the rule map, and one rule will be selected randomly.
     *
     * @param symbol the shape a rule is queried for
     *
     * @return a shape - the _right side_ of the rule
     */
    private Shape selectRule(ShapeSymbol symbol) {
        if (!ruleMap.containsKey(symbol.getLabel()))
            throw new RuntimeException("Symbol " + symbol.toString() + " not in lookup table");

        List<Shape> successorShapes = ruleMap.get(symbol.getLabel());
        if (successorShapes == null) {
            throw new RuntimeException("Symbol " + symbol.toString() + " not in lookup table");
            //return null;
        }
        int index = rand.nextInt(successorShapes.size());
        return successorShapes.get(index).clone();
    }
}
