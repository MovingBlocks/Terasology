/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.github.begla.blockmania.blocks;

import com.github.begla.blockmania.Helper;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;

/**
 * A high grass billboard block.
 * 
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockHighGrass extends Block {

    @Override
    public boolean isBlockTypeTranslucent() {
        return true;
    }

    @Override
    public Vector4f getColorOffsetFor(Block.SIDE side) {
        return new Vector4f(40f / 255f, 190f / 255f, 40f / 255f, 1.0f);
    }

    @Override
    public Vector2f getTextureOffsetFor(Block.SIDE side) {
        return Helper.getInstance().calcOffsetForTextureAt(12, 6);
    }

    @Override
    public boolean isBlockBillboard() {
        return true;
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
}
