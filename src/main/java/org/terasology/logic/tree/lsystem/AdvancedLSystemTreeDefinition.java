package org.terasology.logic.tree.lsystem;

import com.google.common.collect.Maps;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.Time;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.tree.TreeDefinition;
import org.terasology.math.Vector3i;
import org.terasology.utilities.procedural.FastRandom;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class AdvancedLSystemTreeDefinition implements TreeDefinition {
    private final float MAX_ANGLE_OFFSET = (float) Math.PI / 36f;
    private final int GROWTH_INTERVAL = 10000;

    private Map<Character, AxionElementGeneration> blockMap;
    private Map<Character, AxionElementReplacement> axionElementReplacements;
    private List<Block> blockPriorities;
    private float angle;
    private float deathChanceOnGrowth = 0.05f;

    public AdvancedLSystemTreeDefinition(Map<Character, AxionElementReplacement> axionElementReplacements,
                                         Map<Character, AxionElementGeneration> blockMap, List<Block> blockPriorities, float angle) {
        this.axionElementReplacements = axionElementReplacements;
        this.blockMap = blockMap;
        this.blockPriorities = blockPriorities;
        this.angle = angle;
    }

    @Override
    public void updateTree(WorldProvider worldProvider, EntityRef treeRef) {
        LSystemTreeComponent lSystemTree = treeRef.getComponent(LSystemTreeComponent.class);

        long time = CoreRegistry.get(Time.class).getGameTimeInMs();
        if (lSystemTree.lastGrowthTime + GROWTH_INTERVAL < time) {
            Vector3i treeLocation = treeRef.getComponent(BlockComponent.class).getPosition();

            FastRandom rand = new FastRandom();

            if (!lSystemTree.initialized) {
                lSystemTree.branchAngle = this.angle + rand.randomFloat()*MAX_ANGLE_OFFSET;
                lSystemTree.rotationAngle = (float) Math.PI*rand.randomPosFloat();
                lSystemTree.generation = 1;
                lSystemTree.initialized = true;
            }

            Map<Vector3i, Block> currentTree = generateTreeFromAxiom(lSystemTree.axion, lSystemTree.branchAngle, lSystemTree.rotationAngle);

            String nextAxion = generateNextAxion(rand, lSystemTree.axion);

            Map<Vector3i, Block> nextTree = generateTreeFromAxiom(nextAxion, lSystemTree.branchAngle, lSystemTree.rotationAngle);

            updateTreeInGame(worldProvider, treeLocation, currentTree, nextTree);

            lSystemTree.axion = nextAxion;
            lSystemTree.generation++;

            if (rand.randomPosFloat() < deathChanceOnGrowth) {
                treeRef.destroy();
            } else {
                lSystemTree.lastGrowthTime = time;
                treeRef.saveComponent(lSystemTree);
            }
        }
    }

    private void updateTreeInGame(WorldProvider worldProvider, Vector3i treeLocation, Map<Vector3i, Block> currentTree, Map<Vector3i, Block> nextTree) {
        Block air = BlockManager.getAir();

        for (Map.Entry<Vector3i, Block> newTreeBlock : nextTree.entrySet()) {
            Vector3i location = newTreeBlock.getKey();
            Block oldBlock = currentTree.remove(location);
            if (oldBlock != null) {
                worldProvider.setBlock(treeLocation.x + location.x, treeLocation.y + location.y, treeLocation.z + location.z,
                        newTreeBlock.getValue(), oldBlock);
            } else {
                worldProvider.setBlock(treeLocation.x + location.x, treeLocation.y + location.y, treeLocation.z + location.z,
                        newTreeBlock.getValue(), air);
            }
        }

        for (Map.Entry<Vector3i, Block> oldTreeBlock : currentTree.entrySet()) {
            Vector3i location = oldTreeBlock.getKey();
            worldProvider.setBlock(treeLocation.x + location.x, treeLocation.y + location.y, treeLocation.z + location.z,
                    oldTreeBlock.getValue(), air);
        }
    }

    private String generateNextAxion(FastRandom rand, String currentAxion) {
        StringBuilder result = new StringBuilder();
        for (AxionElement axion : parseAxions(currentAxion)) {
            final AxionElementReplacement axionElementReplacement = axionElementReplacements.get(axion.key);
            if (axionElementReplacement != null) {
                result.append(axionElementReplacement.getReplacement(rand.randomPosFloat()));
            } else {
                result.append(axion.key);
                if (axion.parameter != null)
                    result.append("(").append(axion.parameter).append(")");
            }
        }

        return result.toString();
    }

    private Map<Vector3i, Block> generateTreeFromAxiom(String currentAxion, float angleOffset, float treeRotation) {
        Map<Vector3i, Block> treeInMemory = Maps.newHashMap();

        Stack<Vector3f> stackPosition = new Stack<>();
        Stack<Matrix4f> stackOrientation = new Stack<>();

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
        Vector3i blockPosition = new Vector3i(position);
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

    private class Callback implements AxionElementGeneration.AxionElementGenerationCallback {
        private Map<Vector3i, Block> treeInMemory;
        private Vector3f position;
        private Matrix4f rotation;

        private Callback(Map<Vector3i, Block> treeInMemory, Vector3f position, Matrix4f rotation) {
            this.treeInMemory = treeInMemory;
            this.position = position;
            this.rotation = rotation;
        }

        @Override
        public void setBlock(Vector3f position, Block block) {
            AdvancedLSystemTreeDefinition.this.setBlock(treeInMemory, position, block);
        }

        @Override
        public void advance(float distance) {
            Vector3f dir = new Vector3f(0, distance, 0);
            rotation.transform(dir);
            position.add(dir);
        }
    }

    private static class AxionElement {
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

    private static List<AxionElement> parseAxions(String axionString) {
        List<AxionElement> result = new LinkedList<>();
        char[] chars = axionString.toCharArray();
        for (int i = 0, size = chars.length; i < size; i++) {
            char c = chars[i];
            if (c == '(' || c == ')')
                throw new IllegalArgumentException("Invalid axion - parameter without key");
            if (i + 1 < size && chars[i + 1] == '(') {
                int closingBracket = axionString.indexOf(')', i + 1);
                if (closingBracket < 0)
                    throw new IllegalArgumentException("Invalid axion - missing closing bracket");
                String parameter = axionString.substring(i + 2, closingBracket);
                i = closingBracket;
                result.add(new AxionElement(c, parameter));
            } else {
                result.add(new AxionElement(c));
            }
        }

        return result;
    }

}
