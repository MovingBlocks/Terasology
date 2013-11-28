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
    public void generate(AxionElementGenerationCallback callback, Vector3f position, Matrix4f rotation, String axionParameter) {
        callback.setBlock(position, baseBlock);
        int rangeInt = (int) range;
        for (int x = -rangeInt; x <= rangeInt; x++) {
            int y = 0;
            for (int z = -rangeInt; z <= rangeInt; z++) {
                if (Math.sqrt(x * x + y * y + z * z) <= range) {
                    Vector3f v = new Vector3f(x, y, z);
                    rotation.transform(v);
                    Vector3f sideVec = new Vector3f(position);
                    sideVec.add(v);
                    callback.setBlock(sideVec, surroundBlock);
                }
            }
        }

        callback.advance(advance);
    }
}
