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
package org.terasology.core.world.generator.trees;

import org.terasology.math.LSystemRule;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.utilities.collection.CharSequenceIterator;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.chunks.CoreChunk;

import java.util.Map;

/**
 * Allows the generation of complex trees based on L-Systems.
 *
 * @author Benjamin Glatzel
 */
public class TreeGeneratorLSystem extends AbstractTreeGenerator {

    public static final float MAX_ANGLE_OFFSET = (float) Math.toRadians(5);

    /* SETTINGS */
    private int maxDepth;
    private float angle;
    private BlockUri leafType;
    private BlockUri barkType;

    /* RULES */
    private final String initialAxiom;
    private final Map<Character, LSystemRule> ruleSet;

    /**
     * Init. a new L-System based tree generator.
     *
     * @param initialAxiom The initial axiom to use
     * @param ruleSet      The rule set to use
     * @param maxDepth     The maximum recursion depth
     * @param angle        The angle
     */
    public TreeGeneratorLSystem(String initialAxiom, Map<Character, LSystemRule> ruleSet, int maxDepth, float angle) {
        this.angle = angle;
        this.maxDepth = maxDepth;

        this.initialAxiom = initialAxiom;
        this.ruleSet = ruleSet;
    }

    @Override
    public void generate(BlockManager blockManager, CoreChunk view, Random rand, int posX, int posY, int posZ) {
        Vector3f position = new Vector3f(0f, 0f, 0f);

        Matrix4f rotation = new Matrix4f(new Quat4f(new Vector3f(0f, 0f, 1f), (float) Math.PI / 2f), Vector3f.ZERO, 1.0f);

        float angleOffset = rand.nextFloat(-MAX_ANGLE_OFFSET, MAX_ANGLE_OFFSET);

        Block bark = blockManager.getBlock(barkType);
        Block leaf = blockManager.getBlock(leafType);
        recurse(view, rand, posX, posY, posZ, angleOffset, new CharSequenceIterator(initialAxiom),
                position, rotation, bark, leaf, 0);
    }

    private void recurse(CoreChunk view, Random rand, int posX, int posY, int posZ, float angleOffset,
                         CharSequenceIterator axiomIterator, Vector3f position, Matrix4f rotation,
                         Block bark, Block leaf, int depth) {
        Matrix4f tempRotation = new Matrix4f();
        while (axiomIterator.hasNext()) {
            char c = axiomIterator.nextChar();
            switch (c) {
                case 'G':
                case 'F':
                    // Tree trunk

                    safelySetBlock(view, posX + (int) position.x + 1, posY + (int) position.y, posZ + (int) position.z, bark);
                    safelySetBlock(view, posX + (int) position.x - 1, posY + (int) position.y, posZ + (int) position.z, bark);
                    safelySetBlock(view, posX + (int) position.x, posY + (int) position.y, posZ + (int) position.z + 1, bark);
                    safelySetBlock(view, posX + (int) position.x, posY + (int) position.y, posZ + (int) position.z - 1, bark);

                    // Generate leaves
                    if (depth > 1) {
                        int size = 1;

                        for (int x = -size; x <= size; x++) {
                            for (int y = -size; y <= size; y++) {
                                for (int z = -size; z <= size; z++) {
                                    if (Math.abs(x) == size && Math.abs(y) == size && Math.abs(z) == size) {
                                        continue;
                                    }

                                    safelySetBlock(view, posX + (int) position.x + x + 1, posY + (int) position.y + y, posZ + z + (int) position.z, leaf);
                                    safelySetBlock(view, posX + (int) position.x + x - 1, posY + (int) position.y + y, posZ + z + (int) position.z, leaf);
                                    safelySetBlock(view, posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z + 1, leaf);
                                    safelySetBlock(view, posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z - 1, leaf);
                                }
                            }
                        }
                    }

                    Vector3f dir = new Vector3f(1f, 0f, 0f);
                    rotation.transformVector(dir);

                    position.add(dir);
                    break;
                case '[':
                    recurse(view, rand, posX, posY, posZ, angleOffset, axiomIterator, new Vector3f(position), new Matrix4f(rotation), bark, leaf, depth);
                    break;
                case ']':
                    return;
                case '+':
                    tempRotation = new Matrix4f(new Quat4f(new Vector3f(0f, 0f, 1f), angle + angleOffset), Vector3f.ZERO, 1.0f);
                    rotation.mul(tempRotation);
                    break;
                case '-':
                    tempRotation = new Matrix4f(new Quat4f(new Vector3f(0f, 0f, -1f), angle + angleOffset), Vector3f.ZERO, 1.0f);
                    rotation.mul(tempRotation);
                    break;
                case '&':
                    tempRotation = new Matrix4f(new Quat4f(new Vector3f(0f, 1f, 0f), angle + angleOffset), Vector3f.ZERO, 1.0f);
                    rotation.mul(tempRotation);
                    break;
                case '^':
                    tempRotation = new Matrix4f(new Quat4f(new Vector3f(0f, -1f, 0f), angle + angleOffset), Vector3f.ZERO, 1.0f);
                    rotation.mul(tempRotation);
                    break;
                case '*':
                    tempRotation = new Matrix4f(new Quat4f(new Vector3f(1f, 0f, 0f), angle), Vector3f.ZERO, 1.0f);
                    rotation.mul(tempRotation);
                    break;
                case '/':
                    tempRotation = new Matrix4f(new Quat4f(new Vector3f(-1f, 0f, 0f), angle), Vector3f.ZERO, 1.0f);
                    rotation.mul(tempRotation);
                    break;
                default:
                    // If we have already reached the maximum depth, don't ever bother to lookup in the map
                    if (depth == maxDepth - 1) {
                        break;
                    }
                    LSystemRule rule = ruleSet.get(c);
                    if (rule == null) {
                        break;
                    }

                    float weightedFailureProbability = TeraMath.pow(1f - rule.getProbability(), maxDepth - depth);
                    if (rand.nextFloat() < weightedFailureProbability) {
                        break;
                    }

                    recurse(view, rand, posX, posY, posZ, angleOffset, new CharSequenceIterator(rule.getAxiom()),
                            position, rotation, bark, leaf, depth + 1);
            }
        }
    }

    public TreeGeneratorLSystem setLeafType(BlockUri b) {
        leafType = b;
        return this;
    }

    public TreeGeneratorLSystem setBarkType(BlockUri b) {
        barkType = b;
        return this;
    }
}
