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
 */
public class TreeGeneratorLSystem extends AbstractTreeGenerator {

    public static final float MAX_ANGLE_OFFSET = (float) Math.toRadians(5);

    /* SETTINGS */
    private BlockUri leafType;
    private BlockUri barkType;

    /* RULES */
    private final String initialAxiom;
    private RecursiveTreeGeneratorLSystem recursiveGenerator;

    /**
     * Init. a new L-System based tree generator.
     *
     * @param initialAxiom The initial axiom to use
     * @param ruleSet      The rule set to use
     * @param maxDepth     The maximum recursion depth
     * @param angle        The angle
     */
    public TreeGeneratorLSystem(String initialAxiom, Map<Character, LSystemRule> ruleSet, int maxDepth, float angle) {
        this.initialAxiom = initialAxiom;
    
        recursiveGenerator = new RecursiveTreeGeneratorLSystem(maxDepth, angle, ruleSet);
    }

    @Override
    public void generate(BlockManager blockManager, CoreChunk view, Random rand, int posX, int posY, int posZ) {
        Vector3f position = new Vector3f(0f, 0f, 0f);

        Matrix4f rotation = new Matrix4f(new Quat4f(new Vector3f(0f, 0f, 1f), (float) Math.PI / 2f), Vector3f.ZERO, 1.0f);

        float angleOffset = rand.nextFloat(-MAX_ANGLE_OFFSET, MAX_ANGLE_OFFSET);

        Block bark = blockManager.getBlock(barkType);
        Block leaf = blockManager.getBlock(leafType);
        recursiveGenerator.recurse(view, rand, posX, posY, posZ, angleOffset, new CharSequenceIterator(initialAxiom),
                position, rotation, bark, leaf, 0, this);
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
