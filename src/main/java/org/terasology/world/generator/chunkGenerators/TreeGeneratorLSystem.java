/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.generator.chunkGenerators;

import org.terasology.math.LSystemRule;
import org.terasology.utilities.collection.CharSequenceIterator;
import org.terasology.utilities.procedural.FastRandom;
import org.terasology.world.ChunkView;
import org.terasology.world.block.Block;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.Map;

/**
 * Allows the generation of complex trees based on L-Systems.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class TreeGeneratorLSystem extends TreeGenerator {

    public static final float MAX_ANGLE_OFFSET = (float) Math.toRadians(5);

    /* SETTINGS */
    private int maxDepth;
    private float angle;
    private Block leafType;
    private Block barkType;

    /* RULES */
    private final String initialAxiom;
    private final Map<Character, LSystemRule> ruleSet;

    /**
     * Init. a new L-System based tree generator.
     *
     * @param initialAxiom  The initial axiom to use
     * @param ruleSet       The rule set to use
     * @param maxDepth      The maximum recursion depth
     * @param angle         The angle
     */
    public TreeGeneratorLSystem(String initialAxiom, Map<Character, LSystemRule> ruleSet, int maxDepth, float angle) {
        this.angle = angle;
        this.maxDepth = maxDepth;

        this.initialAxiom = initialAxiom;
        this.ruleSet = ruleSet;
    }

    @Override
    public void generate(ChunkView view, FastRandom rand, int posX, int posY, int posZ) {
        Vector3f position = new Vector3f(0f, 0f, 0f);

        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        rotation.setRotation(new AxisAngle4f(0f, 0f, 1f, (float) Math.PI / 2f));

        float angleOffset = rand.randomFloat() * MAX_ANGLE_OFFSET;
        recurse(view, rand, posX, posY, posZ, angleOffset, new CharSequenceIterator(initialAxiom), position, rotation, 0);
    }

    private void recurse(ChunkView view, FastRandom rand, int posX, int posY, int posZ, float angleOffset, CharSequenceIterator axiomIterator, Vector3f position, Matrix4f rotation, int depth) {
        Matrix4f tempRotation = new Matrix4f();
        while (axiomIterator.hasNext()) {
            char c = axiomIterator.nextChar();
            switch (c) {
                case 'G':
                case 'F':
                    // Tree trunk
                    view.setBlock(posX + (int) position.x + 1, posY + (int) position.y, posZ + (int) position.z, barkType);
                    view.setBlock(posX + (int) position.x - 1, posY + (int) position.y, posZ + (int) position.z, barkType);
                    view.setBlock(posX + (int) position.x, posY + (int) position.y, posZ + (int) position.z + 1, barkType);
                    view.setBlock(posX + (int) position.x, posY + (int) position.y, posZ + (int) position.z - 1, barkType);

                    // Generate leaves
                    if (depth > 1) {
                        int size = 1;

                        for (int x = -size; x <= size; x++) {
                            for (int y = -size; y <= size; y++) {
                                for (int z = -size; z <= size; z++) {
                                    if (Math.abs(x) == size && Math.abs(y) == size && Math.abs(z) == size) {
                                        continue;
                                    }

                                    view.setBlock(posX + (int) position.x + x + 1, posY + (int) position.y + y, posZ + z + (int) position.z, leafType);
                                    view.setBlock(posX + (int) position.x + x - 1, posY + (int) position.y + y, posZ + z + (int) position.z, leafType);
                                    view.setBlock(posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z + 1, leafType);
                                    view.setBlock(posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z - 1, leafType);
                                }
                            }
                        }
                    }

                    Vector3f dir = new Vector3f(1f, 0f, 0f);
                    rotation.transform(dir);

                    position.add(dir);
                    break;
                case '[':
                    recurse(view, rand, posX, posY, posZ, angleOffset, axiomIterator, new Vector3f(position), new Matrix4f(rotation), depth);
                    break;
                case ']':
                    return;
                case '+':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(0f, 0f, 1f, angle + angleOffset));
                    rotation.mul(tempRotation);
                    break;
                case '-':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(0f, 0f, -1f, angle + angleOffset));
                    rotation.mul(tempRotation);
                    break;
                case '&':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(0f, 1f, 0f, angle + angleOffset));
                    rotation.mul(tempRotation);
                    break;
                case '^':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(0f, -1f, 0f, angle + angleOffset));
                    rotation.mul(tempRotation);
                    break;
                case '*':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(1f, 0f, 0f, angle));
                    rotation.mul(tempRotation);
                    break;
                case '/':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(-1f, 0f, 0f, angle));
                    rotation.mul(tempRotation);
                    break;
                default:
                    // If we have already reached the maximum depth, don't ever bother to lookup in the map
                    if (depth == maxDepth - 1) break;
                    LSystemRule rule = ruleSet.get(c);
                    if (rule == null) break;

                    // Get a random positive float
                    float randVal = rand.randomFloat();
                    if (randVal < 0f) randVal = -randVal;
                    float weightedFailureProbability = (float) Math.pow(1f - rule.getProbability(), (double) (maxDepth - depth));
                    if (randVal < weightedFailureProbability) break;

                    recurse(view, rand, posX, posY, posZ, angleOffset, new CharSequenceIterator(rule.getAxiom()), position, rotation, depth + 1);
            }
        }
    }

    public TreeGeneratorLSystem setLeafType(Block b) {
        leafType = b;
        return this;
    }

    public TreeGeneratorLSystem setBarkType(Block b) {
        barkType = b;
        return this;
    }
}
