package org.terasology.world.generator.tree;

import com.google.common.collect.Maps;
import org.terasology.math.Vector3i;
import org.terasology.utilities.procedural.FastRandom;
import org.terasology.world.ChunkView;
import org.terasology.world.block.Block;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * This tree growing L-System uses following axion elements:
 * S - sapling (sapling)
 * T - trunk (wood, doesn't create new branches)
 * B - branch (wood, creates new side branches)
 * b - branch small (leaves, can grow into a B)
 * W - blocked (wood, doesn't grow)
 * L - blocked (leaves, doesn't grow)
 * R - removed (nothing, doesn't grow)
 */
public class TreeGeneratorAdvancedLSystem extends TreeGenerator {
    public final float MAX_ANGLE_OFFSET = (float) Math.PI / 36f;

    private Map<Character, AxionElementGeneration> blockMap;
    private String startingAxion;
    private Map<Character, AxionElementReplacement> axionElementReplacements;
    private List<Block> blockPriorities;
    private int generations;
    private float angle;

    public TreeGeneratorAdvancedLSystem(String startingAxion, Map<Character, AxionElementReplacement> axionElementReplacements,
                                        Map<Character, AxionElementGeneration> blockMap, List<Block> blockPriorities, int generations, float angle) {
        this.startingAxion = startingAxion;
        this.axionElementReplacements = axionElementReplacements;
        this.blockMap = blockMap;
        this.blockPriorities = blockPriorities;
        this.generations = generations;
        this.angle = angle;
    }

    @Override
    public void generate(ChunkView view, FastRandom rand, int posX, int posY, int posZ) {
        String currentAxion = startingAxion;
        for (int i = 0; i < generations; i++) {
            StringBuilder result = new StringBuilder();
            for (AxionElement axion : parseAxions(currentAxion)) {
                final AxionElementReplacement axionElementReplacement = axionElementReplacements.get(axion.key);
                if (axionElementReplacement != null) {
                    result.append(axionElementReplacement.getReplacement(nextFloat(rand)));
                } else {
                    result.append(axion.key);
                    if (axion.parameter != null)
                        result.append("(").append(axion.parameter).append(")");
                }
            }

            currentAxion = result.toString();
        }

        Map<Vector3i, Block> treeInMemory = generateTreeFromAxiom(currentAxion, nextFloat(rand) * MAX_ANGLE_OFFSET, (float) (nextFloat(rand) * Math.PI));

        for (Map.Entry<Vector3i, Block> blockAtPosition : treeInMemory.entrySet()) {
            final Vector3i position = blockAtPosition.getKey();

            view.setBlock(posX + position.x, posY + position.y, posZ + position.z, blockAtPosition.getValue());
        }
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

    /**
     * Returns float in range of 0 <= result < 1
     *
     * @param rand
     * @return
     */
    private float nextFloat(FastRandom rand) {
        return (rand.randomFloat() + 1f) / 2f;
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
            TreeGeneratorAdvancedLSystem.this.setBlock(treeInMemory, position, block);
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

    public static void main(String[] args) {
        List<AxionElement> axionElements = parseAxions("da(b)c");
        System.out.println(axionElements.toString());
    }
}
