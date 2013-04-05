/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.world.generator.tree;

import org.terasology.game.CoreRegistry;
import org.terasology.utilities.FastRandom;
import org.terasology.world.ChunkView;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.Map;
import java.util.Stack;

/**
 * Allows the generation of complex trees based on L-Systems.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class TreeGeneratorLSystem extends TreeGenerator {

    public final int MAX_ANGLE_OFFSET = 5;

    /* SETTINGS */
    private int iterations;
    private double angleInDegree;
    private Block air;
    private Block leafType;
    private Block barkType;

    /* RULES */
    private final String initialAxiom;
    private final Map<String, String> ruleSet;
    private final Map<String, Double> probabilities;

    /**
     * Init. a new L-System based tree generator.
     *
     * @param initialAxiom  The initial axiom to use
     * @param ruleSet       The rule set to use
     * @param probabilities The probability array
     * @param iterations    The amount of iterations to execute
     * @param angle         The angle
     */
    public TreeGeneratorLSystem(String initialAxiom, Map<String, String> ruleSet, Map<String, Double> probabilities, int iterations, int angle) {
        angleInDegree = angle;
        this.iterations = iterations;
        air = BlockManager.getAir();
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        leafType = blockManager.getBlock("engine:GreenLeaf");
        barkType = blockManager.getBlock("engine:OakTrunk");

        this.initialAxiom = initialAxiom;
        this.ruleSet = ruleSet;
        this.probabilities = probabilities;
    }

    @Override
    public void generate(ChunkView view, FastRandom rand, int posX, int posY, int posZ) {

        String axiom = initialAxiom;

        Stack<Vector3f> _stackPosition = new Stack<Vector3f>();
        Stack<Matrix4f> _stackOrientation = new Stack<Matrix4f>();

        for (int i = 0; i < iterations; i++) {

            String temp = "";

            for (int j = 0; j < axiom.length(); j++) {
                String c = String.valueOf(axiom.charAt(j));

                double rValue = (rand.randomDouble() + 1.0) / 2.0;

                if (ruleSet.containsKey(c) && probabilities.get(c) > (1.0 - rValue))
                    temp += ruleSet.get(c);
                else
                    temp += c;
            }

            axiom = temp;
        }

        Vector3f position = new Vector3f(0, 0, 0);

        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        rotation.setRotation(new AxisAngle4f(new Vector3f(0, 0, 1), (float) Math.PI / 2.0f));

        int angleOffset = rand.randomInt() % MAX_ANGLE_OFFSET;

        for (int i = 0; i < axiom.length(); i++) {
            char c = axiom.charAt(i);

            Matrix4f tempRotation = new Matrix4f();
            tempRotation.setIdentity();

            switch (c) {
                case 'G':
                case 'F':
                    // Tree trunk
                    view.setBlock(posX + (int) position.x + 1, posY + (int) position.y, posZ + (int) position.z, barkType);
                    view.setBlock(posX + (int) position.x - 1, posY + (int) position.y, posZ + (int) position.z, barkType);
                    view.setBlock(posX + (int) position.x, posY + (int) position.y, posZ + (int) position.z + 1, barkType);
                    view.setBlock(posX + (int) position.x, posY + (int) position.y, posZ + (int) position.z - 1, barkType);

                    // Generate leaves
                    if (_stackOrientation.size() > 1) {
                        int size = 1;

                        for (int x = -size; x <= size; x++) {
                            for (int y = -size; y <= size; y++) {
                                for (int z = -size; z <= size; z++) {
                                    if (Math.abs(x) == size && Math.abs(y) == size && Math.abs(z) == size)
                                        continue;

                                    view.setBlock(posX + (int) position.x + x + 1, posY + (int) position.y + y, posZ + z + (int) position.z, leafType);
                                    view.setBlock(posX + (int) position.x + x - 1, posY + (int) position.y + y, posZ + z + (int) position.z, leafType);
                                    view.setBlock(posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z + 1, leafType);
                                    view.setBlock(posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z - 1, leafType);
                                }
                            }
                        }
                    }

                    Vector3f dir = new Vector3f(1, 0, 0);
                    rotation.transform(dir);

                    position.add(dir);
                    break;
                case '[':
                    _stackOrientation.push(new Matrix4f(rotation));
                    _stackPosition.push(new Vector3f(position));
                    break;
                case ']':
                    rotation = _stackOrientation.pop();
                    position = _stackPosition.pop();
                    break;
                case '+':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, 0, 1), (float) Math.toRadians(angleInDegree + angleOffset)));
                    rotation.mul(tempRotation);
                    break;
                case '-':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, 0, -1), (float) Math.toRadians(angleInDegree + angleOffset)));
                    rotation.mul(tempRotation);
                    break;
                case '&':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, 1, 0), (float) Math.toRadians(angleInDegree + angleOffset)));
                    rotation.mul(tempRotation);
                    break;
                case '^':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, -1, 0), (float) Math.toRadians(angleInDegree + angleOffset)));
                    rotation.mul(tempRotation);
                    break;
                case '*':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(1, 0, 0), (float) Math.toRadians(angleInDegree)));
                    rotation.mul(tempRotation);
                    break;
                case '/':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(-1, 0, 0), (float) Math.toRadians(angleInDegree)));
                    rotation.mul(tempRotation);
                    break;
            }
        }
    }

    public TreeGenerator setLeafType(Block b) {
        leafType = b;
        return this;
    }


    public TreeGenerator setBarkType(Block b) {
        barkType = b;
        return this;
    }
}
