/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.logic.generators;

import org.terasology.model.blocks.management.BlockManager;
import org.terasology.utilities.FastRandom;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Stack;

/**
 * Allows the generation of complex trees based on L-Systems.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class TreeGeneratorLSystem extends TreeGenerator {

    public final int MAX_ANGLE_OFFSET = 5;

    /* SETTINGS */
    private int _iterations;
    private double _angleInDegree;
    private byte _leafType;
    private byte _barkType;

    /* RULES */
    private final String _initialAxiom;
    private final HashMap<String, String> _ruleSet;
    private final HashMap<String, Double> _probabilities;

    /**
     * Init. a new L-System based tree generator.
     *
     * @param manager       The generator manager
     * @param initialAxiom  The initial axiom to use
     * @param ruleSet       The rule set to use
     * @param probabilities The probability array
     * @param iterations    The amount of iterations to execute
     * @param angle         The angle
     */
    public TreeGeneratorLSystem(GeneratorManager manager, String initialAxiom, HashMap<String, String> ruleSet, HashMap<String, Double> probabilities, int iterations, int angle) {
        super(manager);

        _angleInDegree = angle;
        _iterations = iterations;
        _leafType = BlockManager.getInstance().getBlock("GreenLeaf").getId();
        _barkType = BlockManager.getInstance().getBlock("OakTrunk").getId();

        _initialAxiom = initialAxiom;
        _ruleSet = ruleSet;
        _probabilities = probabilities;
    }

    @Override
    public void generate(FastRandom rand, int posX, int posY, int posZ, boolean update) {

        String axiom = _initialAxiom;

        Stack<Vector3f> _stackPosition = new Stack<Vector3f>();
        Stack<Matrix4f> _stackOrientation = new Stack<Matrix4f>();

        for (int i = 0; i < _iterations; i++) {

            String temp = "";

            for (int j = 0; j < axiom.length(); j++) {
                String c = String.valueOf(axiom.charAt(j));

                double rValue = (rand.randomDouble() + 1.0) / 2.0;

                if (_ruleSet.containsKey(c) && _probabilities.get(c) > (1.0 - rValue))
                    temp += _ruleSet.get(c);
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
                    _generatorManager.getParent().setBlock(posX + (int) position.x + 1, posY + (int) position.y, posZ + (int) position.z, _barkType, update, true, true);
                    _generatorManager.getParent().setBlock(posX + (int) position.x - 1, posY + (int) position.y, posZ + (int) position.z, _barkType, update, true, true);
                    _generatorManager.getParent().setBlock(posX + (int) position.x, posY + (int) position.y, posZ + (int) position.z + 1, _barkType, update, true, true);
                    _generatorManager.getParent().setBlock(posX + (int) position.x, posY + (int) position.y, posZ + (int) position.z - 1, _barkType, update, true, true);


                    // Generate leaves
                    if (_stackOrientation.size() > 1) {
                        int size = 1;

                        for (int x = -size; x <= size; x++) {
                            for (int y = -size; y <= size; y++) {
                                for (int z = -size; z <= size; z++) {
                                    if (Math.abs(x) == size && Math.abs(y) == size && Math.abs(z) == size)
                                        continue;

                                    if (_generatorManager.getParent().getBlock(posX + (int) position.x + x + 1, posY + (int) position.y + y, posZ + z + (int) position.z) == 0x0)
                                        _generatorManager.getParent().setBlock(posX + (int) position.x + x + 1, posY + (int) position.y + y, posZ + z + (int) position.z, _leafType, update, true, true);

                                    if (_generatorManager.getParent().getBlock(posX + (int) position.x + x - 1, posY + (int) position.y + y, posZ + z + (int) position.z) == 0x0)
                                        _generatorManager.getParent().setBlock(posX + (int) position.x + x - 1, posY + (int) position.y + y, posZ + z + (int) position.z, _leafType, update, true, true);

                                    if (_generatorManager.getParent().getBlock(posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z + 1) == 0x0)
                                        _generatorManager.getParent().setBlock(posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z + 1, _leafType, update, true, true);

                                    if (_generatorManager.getParent().getBlock(posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z - 1) == 0x0)
                                        _generatorManager.getParent().setBlock(posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z - 1, _leafType, update, true, true);

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
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, 0, 1), (float) Math.toRadians(_angleInDegree + angleOffset)));
                    rotation.mul(tempRotation);
                    break;
                case '-':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, 0, -1), (float) Math.toRadians(_angleInDegree + angleOffset)));
                    rotation.mul(tempRotation);
                    break;
                case '&':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, 1, 0), (float) Math.toRadians(_angleInDegree + angleOffset)));
                    rotation.mul(tempRotation);
                    break;
                case '^':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, -1, 0), (float) Math.toRadians(_angleInDegree + angleOffset)));
                    rotation.mul(tempRotation);
                    break;
                case '*':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(1, 0, 0), (float) Math.toRadians(_angleInDegree)));
                    rotation.mul(tempRotation);
                    break;
                case '/':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(-1, 0, 0), (float) Math.toRadians(_angleInDegree)));
                    rotation.mul(tempRotation);
                    break;
            }
        }
    }

    public TreeGenerator withLeafType(byte b) {
        _leafType = b;
        return this;
    }


    public TreeGenerator withBarkType(byte b) {
        _barkType = b;
        return this;
    }
}
