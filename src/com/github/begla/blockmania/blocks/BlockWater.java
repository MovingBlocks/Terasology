/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.blocks;

import com.github.begla.blockmania.utilities.Helper;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;

/**
 * A water block.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockWater extends Block {

    private static Vector4f colorOffset = new Vector4f(0.9f, 0.9f, 1.0f, 0.7f);

    @Override
    public boolean isBlockTypeTranslucent() {
        return true;
    }

    @Override
    public Vector4f getColorOffsetFor(SIDE side) {
        return colorOffset;
    }

    @Override
    public Vector2f getTextureOffsetFor(Block.SIDE side) {
        return Helper.calcOffsetForTextureAt(15, 13);
    }

    @Override
    public boolean isPenetrable() {
        return true;
    }

    @Override
    public boolean isCastingShadows() {
        return false;
    }

    @Override
    public boolean renderBoundingBox() {
        return false;
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public BLOCK_FORM getBlockForm() {
        return BLOCK_FORM.LOWERED_BOCK;
    }
}
