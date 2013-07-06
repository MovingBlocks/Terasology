package org.terasology.world.generator.tree;

import org.terasology.math.Rotation;
import org.terasology.world.block.Block;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class DefaultAxionElementGeneration implements AxionElementGeneration {
    private Block block;
    private float advance;

    public DefaultAxionElementGeneration(Block block, float advance) {
        this.block = block;
        this.advance = advance;
    }

    @Override
    public void generate(AxionElementGenerationCallback callback, Vector3f position, Matrix4f rotation) {
        callback.setBlock(position, block);
        callback.advance(advance);
    }
}
