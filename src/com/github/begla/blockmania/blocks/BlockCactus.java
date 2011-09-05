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

/**
 * A cactus block.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockCactus extends Block {

    @Override
    public boolean doNotTessellate() {
        return true;
    }

    @Override
    public boolean isBlockTypeTranslucent() {
        return true;
    }

    @Override
    public Vector2f getTextureOffsetFor(SIDE side) {
        if (side == Block.SIDE.LEFT || side == Block.SIDE.RIGHT || side == Block.SIDE.FRONT || side == Block.SIDE.BACK) {
            return Helper.calcOffsetForTextureAt(6, 4);
        }

        return Helper.calcOffsetForTextureAt(5, 4);
    }

    public BLOCK_FORM getBlockForm() {
        return BLOCK_FORM.CACTUS;
    }
}
