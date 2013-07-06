package org.terasology.world.generator.tree;

import org.terasology.math.Rotation;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface AxionElementGeneration {
    public void generate(AxionElementGenerationCallback callback, Vector3f position, Matrix4f rotation);

    public interface AxionElementGenerationCallback {
        public void setBlock(Vector3f position, Block block);
        public void advance(float distance);
    }
}
