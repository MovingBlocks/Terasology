/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.core.logic.tree.lsystem;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.core.logic.tree.LivingTreeComponent;
import org.terasology.core.logic.tree.TreeDefinition;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Vector3i;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class AdvancedLSystemTreeDefinition implements TreeDefinition {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedLSystemTreeDefinition.class);

    private static final int GROWTH_SAFE_DISTANCE = 25;
    private static final float MAX_ANGLE_OFFSET = (float) Math.PI / 18f;
    private static final int GROWTH_INTERVAL = 30 * 1000;

    private Map<Character, AxionElementGeneration> blockMap;
    private Map<Character, AxionElementReplacement> axionElementReplacements;
    private List<Block> blockPriorities;
    private float angle;
    private int minGenerations = 30;
    private int maxGenerations = 45;

    public AdvancedLSystemTreeDefinition(Map<Character, AxionElementReplacement> axionElementReplacements,
                                         Map<Character, AxionElementGeneration> blockMap, List<Block> blockPriorities, float angle) {
        this.axionElementReplacements = axionElementReplacements;
        this.blockMap = blockMap;
        this.blockPriorities = blockPriorities;
        this.angle = angle;
    }

    @Override
    public void updateTree(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef treeRef) {
        LSystemTreeComponent lSystemTree = treeRef.getComponent(LSystemTreeComponent.class);

        if (lSystemTree != null) {
            long time = CoreRegistry.get(Time.class).getGameTimeInMs();

            if (shouldInitializeSapling(lSystemTree)) {
                logger.debug("Initializing sapling");

                FastRandom rand = new FastRandom();

                lSystemTree.branchAngle = rand.nextFloat(-MAX_ANGLE_OFFSET, MAX_ANGLE_OFFSET);
                lSystemTree.rotationAngle = (float) Math.PI * rand.nextFloat();
                lSystemTree.generation = 1;
                lSystemTree.initialized = true;
                // Update time when sapling was placed
                lSystemTree.lastGrowthTime = time;

                treeRef.saveComponent(lSystemTree);
            } else if (shouldProcessTreeGrowth(lSystemTree, time)) {
                Vector3i treeLocation = treeRef.getComponent(BlockComponent.class).getPosition();

                FastRandom rand = new FastRandom();

                if (hasRoomToGrow(worldProvider, treeLocation)) {
                    logger.debug("Growing tree");

                    Map<Vector3i, Block> currentTree = generateTreeFromAxiom(lSystemTree.axion, lSystemTree.branchAngle, lSystemTree.rotationAngle);

                    logger.debug("Starting axion generation");
                    String nextAxion;
                    if (lSystemTree.generated) {
                        lSystemTree.generated = false;
                        int generation = rand.nextInt(maxGenerations);
                        nextAxion = lSystemTree.axion;
                        for (int i = 0; i < generation; i++) {
                            nextAxion = generateNextAxion(rand, nextAxion);
                        }
                        lSystemTree.generation = generation;
                    } else {
                        nextAxion = generateNextAxion(rand, lSystemTree.axion);
                    }
                    logger.debug("Finished axion generation");

                    Map<Vector3i, Block> nextTree = generateTreeFromAxiom(nextAxion, lSystemTree.branchAngle, lSystemTree.rotationAngle);

                    logger.debug("Starting replacement of blocks");
                    updateTreeInGame(worldProvider, blockEntityRegistry, treeLocation, currentTree, nextTree);
                    logger.debug("Finished replacement of blocks");

                    lSystemTree.axion = nextAxion;
                    lSystemTree.generation++;

                    logger.debug("Generation: " + lSystemTree.generation + ", tree: " + treeLocation);

                    if (checkForDeath(lSystemTree.generation, rand.nextFloat())) {
                        treeRef.removeComponent(LSystemTreeComponent.class);
                    } else {
                        lSystemTree.lastGrowthTime = time + rand.nextInt(GROWTH_INTERVAL / 2);
                        treeRef.saveComponent(lSystemTree);
                    }
                }
            }
        }
    }

    private boolean shouldInitializeSapling(LSystemTreeComponent lSystemTree) {
        return !lSystemTree.initialized;
    }

    private boolean shouldProcessTreeGrowth(LSystemTreeComponent lSystemTree, long time) {
        logger.debug("Considering processing tree, last growth: " + lSystemTree.lastGrowthTime + ", current time: " + time);
        return lSystemTree.generated || lSystemTree.lastGrowthTime + GROWTH_INTERVAL < time;
    }

    private boolean hasRoomToGrow(WorldProvider worldProvider, Vector3i treeLocation) {
        return worldProvider.isBlockRelevant(treeLocation.x + GROWTH_SAFE_DISTANCE, treeLocation.y, treeLocation.z + GROWTH_SAFE_DISTANCE)
                && worldProvider.isBlockRelevant(treeLocation.x - GROWTH_SAFE_DISTANCE, treeLocation.y, treeLocation.z - GROWTH_SAFE_DISTANCE);
    }

    private boolean checkForDeath(int generation, float random) {
        if (generation < minGenerations) {
            return false;
        }
        double deathChance = Math.pow(1f * (maxGenerations - generation) / (maxGenerations - minGenerations), 0.2);
//        logger.debug("Death chance: " + ((1 - deathChance) * 100) + "%");
        return (deathChance < random);
    }

    private void updateTreeInGame(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i treeLocation,
                                  Map<Vector3i, Block> currentTree, Map<Vector3i, Block> nextTree) {
        Block air = BlockManager.getAir();

        int replaceCount = 0;
        final Vector3i origin = Vector3i.zero();

        for (Map.Entry<Vector3i, Block> newTreeBlock : nextTree.entrySet()) {
            Vector3i relativeLocation = newTreeBlock.getKey();
            Block oldBlock = currentTree.remove(relativeLocation);
            Block newBlock = newTreeBlock.getValue();
            Vector3i blockLocation = new Vector3i(treeLocation.x + relativeLocation.x, treeLocation.y + relativeLocation.y, treeLocation.z + relativeLocation.z);
            Block resultBlock = newBlock.getBlockFamily().getBlockForPlacement(worldProvider, blockEntityRegistry, blockLocation, null, null);

            if (oldBlock != null && oldBlock != newBlock) {
                if (relativeLocation.equals(origin)) {
                    blockEntityRegistry.setBlockRetainComponent(blockLocation, resultBlock, LSystemTreeComponent.class, LivingTreeComponent.class);
                } else {
                    Block block = worldProvider.getBlock(blockLocation);
                    if (block.isReplacementAllowed() || block.getBlockFamily() == oldBlock.getBlockFamily())
                        worldProvider.setBlock(blockLocation, resultBlock);
                }
                replaceCount++;
            } else if (oldBlock == null) {
                if (worldProvider.getBlock(blockLocation).isReplacementAllowed())
                    worldProvider.setBlock(blockLocation, resultBlock);
                replaceCount++;
            }
        }

        for (Map.Entry<Vector3i, Block> oldTreeBlock : currentTree.entrySet()) {
            Vector3i location = oldTreeBlock.getKey();
            worldProvider.setBlock(new Vector3i(treeLocation.x + location.x, treeLocation.y + location.y, treeLocation.z + location.z),
                    air);
            replaceCount++;
        }

        logger.debug("Replaced block count: " + replaceCount);
    }

    private String generateNextAxion(FastRandom rand, String currentAxion) {
        StringBuilder result = new StringBuilder();
        for (AxionElement axion : parseAxions(currentAxion)) {
            final AxionElementReplacement axionElementReplacement = axionElementReplacements.get(axion.key);
            if (axionElementReplacement != null) {
                result.append(axionElementReplacement.getReplacement(rand.nextFloat(), currentAxion));
            } else {
                result.append(axion.key);
                if (axion.parameter != null) {
                    result.append("(").append(axion.parameter).append(")");
                }
            }
        }

        return result.toString();
    }

    private Map<Vector3i, Block> generateTreeFromAxiom(String currentAxion, float angleOffset, float treeRotation) {
        Map<Vector3i, Block> treeInMemory = Maps.newHashMap();

        Deque<Vector3f> stackPosition = Queues.newArrayDeque();
        Deque<Matrix4f> stackOrientation = Queues.newArrayDeque();

        Vector3f position = new Vector3f(0, 0, 0);
        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        rotation.rotY(treeRotation);

        Callback callback = new Callback(treeInMemory, position, rotation);

        for (AxionElement axion : parseAxions(currentAxion)) {
            Matrix4f tempRotation = new Matrix4f();
            tempRotation.setIdentity();

            char c = axion.key;
            switch (c) {
                case '[':
                    stackOrientation.push(new Matrix4f(rotation));
                    stackPosition.push(new Vector3f(position));
                    break;
                case ']':
                    rotation.set(stackOrientation.pop());
                    position.set(stackPosition.pop());
                    break;
                case '&':
                    tempRotation.setIdentity();
                    tempRotation.rotX(angle + angleOffset);
                    rotation.mul(tempRotation);
                    break;
                case '^':
                    tempRotation.setIdentity();
                    tempRotation.rotX(-angle - angleOffset);
                    rotation.mul(tempRotation);
                    break;
                case '+':
                    tempRotation.setIdentity();
                    tempRotation.rotY((float) Math.toRadians(Integer.parseInt(axion.parameter)));
                    rotation.mul(tempRotation);
                    break;
                case '-':
                    tempRotation.setIdentity();
                    tempRotation.rotY(-(float) Math.toRadians(Integer.parseInt(axion.parameter)));
                    rotation.mul(tempRotation);
                    break;
                default:
                    AxionElementGeneration axionElementGeneration = blockMap.get(c);
                    if (axionElementGeneration != null) {
                        axionElementGeneration.generate(callback, position, rotation, axion.parameter);
                    }
            }
        }
        return treeInMemory;
    }

    private void setBlock(Map<Vector3i, Block> treeInMemory, Vector3f position, Block block) {
        Vector3i blockPosition = new Vector3i(position.x + 0.5f, position.y + 0.5f, position.z + 0.5f);
        if (blockPosition.y >= 0) {
            final Block blockAtPosition = treeInMemory.get(blockPosition);
            if (blockAtPosition == block || hasBlockWithHigherPriority(block, blockAtPosition)) {
                return;
            }
            treeInMemory.put(blockPosition, block);
        }
    }

    private boolean hasBlockWithHigherPriority(Block block, Block blockAtPosition) {
        return blockAtPosition != null && blockPriorities.indexOf(blockAtPosition) < blockPriorities.indexOf(block);
    }

    private static List<AxionElement> parseAxions(String axionString) {
        List<AxionElement> result = new LinkedList<>();
        char[] chars = axionString.toCharArray();
        int index = 0;
        int size = chars.length;
        while (index < size) {
            char c = chars[index];
            if (c == '(' || c == ')') {
                throw new IllegalArgumentException("Invalid axion - parameter without key");
            }
            if (index + 1 < size && chars[index + 1] == '(') {
                int closingBracket = axionString.indexOf(')', index + 1);
                if (closingBracket < 0) {
                    throw new IllegalArgumentException("Invalid axion - missing closing bracket");
                }
                String parameter = axionString.substring(index + 2, closingBracket);
                index = closingBracket;
                result.add(new AxionElement(c, parameter));
            } else {
                result.add(new AxionElement(c));
            }
            index++;
        }

        return result;
    }

    private final class Callback implements AxionElementGeneration.AxionElementGenerationCallback {
        private Map<Vector3i, Block> treeInMemory;
        private Vector3f position;
        private Matrix4f rotation;

        private Callback(Map<Vector3i, Block> treeInMemory, Vector3f position, Matrix4f rotation) {
            this.treeInMemory = treeInMemory;
            this.position = position;
            this.rotation = rotation;
        }

        @Override
        public void setBlock(Vector3f blockPosition, Block block) {
            AdvancedLSystemTreeDefinition.this.setBlock(treeInMemory, blockPosition, block);
        }

        @Override
        public void advance(float distance) {
            Vector3f dir = new Vector3f(0, distance, 0);
            rotation.transform(dir);
            position.add(dir);
        }
    }

    private static final class AxionElement {
        private char key;
        private String parameter;

        private AxionElement(char key, String parameter) {
            this.key = key;
            this.parameter = parameter;
        }

        private AxionElement(char key) {
            this.key = key;
        }
    }
}
