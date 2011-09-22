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
 * A (dark) leaf block.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockDarkLeaf extends Block {

    @Override
    public boolean isCastingShadows() {
        return true;
    }

    @Override
    public boolean isBlockTypeTranslucent() {
        return true;
    }

    @Override
    public Vector4f getColorOffsetFor(SIDE side, double temp, double hum) {
        Vector4f leafColor = foliageColorForTemperatureAndHumidity(temp, hum);
        return new Vector4f(leafColor.x * 0.8f, leafColor.y * 0.8f, leafColor.z * 0.8f, 1.0f);
    }

    @Override
    public Vector2f getTextureOffsetFor(Block.SIDE side) {
        return Helper.calcOffsetForTextureAt(4, 3);
    }

    /**
     * @return
     */
    @Override
    public boolean doNotTessellate() {
        return true;
    }
}
