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
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

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
    private boolean _generateLeafBlocks = true;
    private byte _leafType;

    /* RULES */
    private String _initialAxiom;
    private HashMap<String, String> _ruleSet;

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
        _iterations = 3;
        _leafType = BlockManager.getInstance().getBlock("Leaf").getId();

        _initialAxiom = initialAxiom;
        _ruleSet = ruleSet;
    }

    @Override
    public void generate(FastRandom rand, int posX, int posY, int posZ, boolean update) {

        String axiom = new String(_initialAxiom);

        Stack<Vector4f> _stackPosition = new Stack<Vector4f>();
        Stack<Matrix4f> _stackOrientation = new Stack<Matrix4f>();

        for (int i = 0; i < _iterations; i++) {

            String temp = "";

            for (int j = 0; j < axiom.length(); j++) {
                char c = axiom.charAt(j);

                for (String a : _ruleSet.keySet()) {
                    if (a.charAt(0) == c) {
                        temp += _ruleSet.get(a);
                        continue;
                    }
                }

                temp += c;
            }

            axiom = temp;
        }

        Vector4f position = new Vector4f(0, 0, 0, 1);
        Matrix4f rotation = new Matrix4f();
        rotation.rotate((float) Math.PI / 2, new Vector3f(0, 0, 1));

        beforeExecution(rand);

        for (int i = 0; i < axiom.length(); i++) {
            char c = axiom.charAt(i);

            switch (c) {
                case 'G':
                case 'F':
                    // Tree trunk
                    _generatorManager.getParent().setBlock(posX + (int) position.x, posY + (int) position.y, posZ + (int) position.z, BlockManager.getInstance().getBlock("Tree trunk").getId(), update, true);

                    // Generate leafs
                    if (_stackOrientation.size() > 2 && _generateLeafBlocks) {
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

                    Vector4f dir = new Vector4f(1, 0, 0, 1);
                    Matrix4f.transform(rotation, dir, dir);

                    position.x += dir.x;
                    position.y += dir.y;
                    position.z += dir.z;
                    break;
                case '[':
                    _stackOrientation.push(new Matrix4f(rotation));
                    _stackPosition.push(new Vector4f(position));
                    break;
                case ']':
                    rotation = _stackOrientation.pop();
                    position = _stackPosition.pop();
                    break;
                case '+':
                    rotation.rotate((float) Math.toRadians(_angleInDegree), new Vector3f(0, 0, 1));
                    break;
                case '-':
                    rotation.rotate((float) Math.toRadians(_angleInDegree), new Vector3f(0, 0, 1));
                    break;
                case '&':
                    rotation.rotate((float) Math.toRadians(_angleInDegree), new Vector3f(0, 1, 0));
                    break;
                case '^':
                    rotation.rotate((float) Math.toRadians(_angleInDegree), new Vector3f(0, 1, 0));
                    break;
                case '*':
                    rotation.rotate((float) Math.toRadians(_angleInDegree), new Vector3f(1, 0, 0));
                    break;
                case '/':
                    rotation.rotate((float) Math.toRadians(_angleInDegree), new Vector3f(1, 0, 0));
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
