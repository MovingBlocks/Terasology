package org.terasology.world.generator.tree;

import com.google.common.collect.Maps;
import org.terasology.math.Vector3i;
import org.terasology.utilities.procedural.FastRandom;
import org.terasology.world.ChunkView;
import org.terasology.world.block.Block;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
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
    public final float MAX_ANGLE_OFFSET = (float) Math.PI / 36;

    private Map<Character, Block> blockMap;
    private String startingAxion;
    private Map<Character, AxionElementReplacement> axionElementReplacements;
    private List<Block> blockPriorities;
    private int generations;
    private float angle;

    public TreeGeneratorAdvancedLSystem(String startingAxion, Map<Character, AxionElementReplacement> axionElementReplacements,
                                        Map<Character, Block> blockMap, List<Block> blockPriorities, int generations, float angle) {
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
            for (char axionChar : currentAxion.toCharArray()) {
                final AxionElementReplacement axionElementReplacement = axionElementReplacements.get(axionChar);
                if (axionElementReplacement != null) {
                    result.append(axionElementReplacement.getReplacement(nextFloat(rand)));
                } else {
                    result.append(axionChar);
                }
            }

            currentAxion = result.toString();
        }

        Map<Vector3i, Block> treeInMemory = generateTreeFromAxiom(currentAxion, nextFloat(rand) * MAX_ANGLE_OFFSET);

        for (Map.Entry<Vector3i, Block> blockAtPosition: treeInMemory.entrySet()){
            final Vector3i position = blockAtPosition.getKey();

            view.setBlock(posX+position.x, posY+position.y, posZ+position.z, blockAtPosition.getValue());
        }
    }

    private Map<Vector3i, Block> generateTreeFromAxiom(String currentAxion, float angleOffset) {
        Map<Vector3i, Block> treeInMemory = Maps.newHashMap();

        Stack<Vector3f> stackPosition = new Stack<>();
        Stack<Matrix4f> stackOrientation = new Stack<>();

        Vector3f position = new Vector3f(0, 0, 0);
        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();

        for (char c : currentAxion.toCharArray()) {
            Matrix4f tempRotation = new Matrix4f();
            tempRotation.setIdentity();

            switch (c) {
                case '[':
                    stackOrientation.push(new Matrix4f(rotation));
                    stackPosition.push(new Vector3f(position));
                    break;
                case ']':
                    rotation = stackOrientation.pop();
                    position = stackPosition.pop();
                    break;
                case '+':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, 0, 1), angle + angleOffset));
                    rotation.mul(tempRotation);
                    break;
                case '-':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, 0, -1), angle + angleOffset));
                    rotation.mul(tempRotation);
                    break;
                case '&':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, 1, 0), angle + angleOffset));
                    rotation.mul(tempRotation);
                    break;
                case '^':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(0, -1, 0), angle + angleOffset));
                    rotation.mul(tempRotation);
                    break;
                case '*':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(1, 0, 0), angle + angleOffset));
                    rotation.mul(tempRotation);
                    break;
                case '/':
                    tempRotation.setIdentity();
                    tempRotation.setRotation(new AxisAngle4f(new Vector3f(-1, 0, 0), angle + angleOffset));
                    rotation.mul(tempRotation);
                    break;
                default:
                    setBlock(treeInMemory, position, blockMap.get(c));

                    // Tree grows up by default
                    Vector3f dir = new Vector3f(0, 1, 0);
                    rotation.transform(dir);

                    position.add(dir);
                    break;
            }
        }
        return treeInMemory;
    }

    /**
     * Returns float in range of 0 <= result < 1
     * @param rand
     * @return
     */
    private float nextFloat(FastRandom rand) {
        return (rand.randomFloat() + 1f) / 2f;
    }

    private void setBlock(Map<Vector3i, Block> treeInMemory, Vector3f position, Block block) {
        Vector3i blockPosition = new Vector3i(position);
        final Block blockAtPosition = treeInMemory.get(blockPosition);
        if (blockAtPosition == block || hasBlockWithHigherPriority(block, blockAtPosition)) {
            return;
        }
        treeInMemory.put(blockPosition, block);
    }

    private boolean hasBlockWithHigherPriority(Block block, Block blockAtPosition) {
        return blockAtPosition != null && blockPriorities.indexOf(blockAtPosition) < blockPriorities.indexOf(block);
    }
}
