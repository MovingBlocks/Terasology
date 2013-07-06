package org.terasology.world.generator.tree;

import org.terasology.math.TeraMath;
import org.terasology.world.block.Block;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SurroundAxionElementGeneration implements AxionElementGeneration {
    private Block baseBlock;
    private Block surroundBlock;
    private float advance;
    private float range;

    public SurroundAxionElementGeneration(Block baseBlock, Block surroundBlock, float advance) {
        this(baseBlock, surroundBlock, advance, 1);
    }

    public SurroundAxionElementGeneration(Block baseBlock, Block surroundBlock, float advance, float range) {
        this.baseBlock = baseBlock;
        this.surroundBlock = surroundBlock;
        this.advance = advance;
        this.range = range;
    }

    @Override
    public void generate(AxionElementGenerationCallback callback, Vector3f position, Matrix4f rotation) {
        callback.setBlock(position, baseBlock);
        int rangeInt = (int) range;
        for (int x = -rangeInt; x <= rangeInt; x++) {
            int y=0;
//            for (int y = -rangeInt; y <= rangeInt; y++) {
                for (int z = -rangeInt; z <= rangeInt; z++) {
                    if (Math.sqrt(x * x + y * y + z * z) < range) {
                        Vector3f v = new Vector3f(x, y, z);
                        rotation.transform(v);
                        Vector3f sideVec = new Vector3f(position);
                        sideVec.add(v);
                        callback.setBlock(sideVec, surroundBlock);
                    }
                }
//            }
        }

        callback.advance(advance);
    }
}
