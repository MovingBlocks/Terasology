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
package com.github.begla.blockmania.generators;

import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.utilities.FastRandom;

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

    /* SETTINGS */
    private int _iterations;
    private double _angleInDegree;
    private byte _leafType;

    /* RULES */
    private final String _initialAxiom;
    private final HashMap<String, String> _ruleSet;

    /**
     * Init. a new L-System based tree generator.
     *
     * @param manager      The generator manager
     * @param initialAxiom The initial axiom to use
     * @param ruleSet      The rule set to use
     */
    public TreeGeneratorLSystem(GeneratorManager manager, String initialAxiom, HashMap<String, String> ruleSet) {
        super(manager);

        _angleInDegree = 20;
        _iterations = 6;
        _leafType = BlockManager.getInstance().getBlock("Leaf").getId();

        _initialAxiom = initialAxiom;
        _ruleSet = ruleSet;
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

                if (_ruleSet.containsKey(c))
                    temp += _ruleSet.get(c);
                else
                    temp += c;
            }

            axiom = temp;
        }

        Vector3f position = new Vector3f(0, 0, 0);

        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        rotation.setRotation(new AxisAngle4f(new Vector3f(0, 0, 1), (float) Math.PI / 2));

        beforeExecution(rand);

        for (int i = 0; i < axiom.length(); i++) {
            char c = axiom.charAt(i);

            Matrix4f tempRotation = new Matrix4f();
            tempRotation.setIdentity();

            switch (c) {
                case 'G':
                case 'F':
                    // Tree trunk
                    _generatorManager.getParent().setBlock(posX + (int) position.x, posY + (int) position.y, posZ + (int) position.z, BlockManager.getInstance().getBlock("Tree trunk").getId(), update, true);

                    // Generate leafs
                    if (_stackOrientation.size() > 1) {
                        int size = 1;

                        for (int x = -size; x <= size; x++) {
                            for (int y = -size; y <= size; y++) {
                                for (int z = -size; z <= size; z++) {
                                    if (Math.abs(x) == size && Math.abs(y) == size && Math.abs(z) == size)
                                        continue;

                                    if (_generatorManager.getParent().getBlock(posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z) == 0x0)
                                        _generatorManager.getParent().setBlock(posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z, _leafType, update, false);

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
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, 0, 1), (float) Math.toRadians(_angleInDegree)));
                    rotation.mul(tempRotation);
                    break;
                case '-':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, 0, -1), (float) Math.toRadians(_angleInDegree)));
                    rotation.mul(tempRotation);
                    break;
                case '&':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, 1, 0), (float) Math.toRadians(_angleInDegree)));
                    rotation.mul(tempRotation);
                    break;
                case '^':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, -1, 0), (float) Math.toRadians(_angleInDegree)));
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

    private void beforeExecution(FastRandom rand) {
        _angleInDegree = 30 + rand.randomDouble() * 10;
        _iterations = Math.abs(rand.randomInt() % 2) + 4;
    }

    public TreeGenerator withLeafType(byte b) {
        _leafType = b;
        return this;
    }
}
