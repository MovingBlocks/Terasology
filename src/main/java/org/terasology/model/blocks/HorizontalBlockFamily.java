/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.model.blocks;

import org.terasology.math.Side;

import java.util.EnumMap;

/**
 * Block group for blocks that can be oriented around the vertical axis.
 *
 * @author Immortius <immortius@gmail.com>
 */
public class HorizontalBlockFamily implements BlockFamily {

    String _name;
    EnumMap<Side, Block> _blocks = new EnumMap<Side, Block>(Side.class);

    /**
     * @param name   The name for the block group.
     * @param blocks The set of blocks that make up the group. Front, Back, Left and Right must be provided - the rest is ignored.
     */
    public HorizontalBlockFamily(String name, EnumMap<Side, Block> blocks) {
        _name = name;
        for (Side side : Side.horizontalSides()) {
            Block block = blocks.get(side);
            if (block == null) {
                throw new IllegalArgumentException("Missing block for side: " + side.toString());
            }
            _blocks.put(side, block);
            block.withBlockFamily(this);
        }
    }

    public String getTitle() {
        return _name;
    }

    public byte getBlockIdFor(Side attachmentSide, Side direction) {
        return getBlockFor(attachmentSide, direction).getId();
    }

    public Block getBlockFor(Side attachmentSide, Side direction) {
        if (attachmentSide.isHorizontal()) {
            return _blocks.get(attachmentSide);
        }
        return _blocks.get(direction);

    }

    public Block getArchetypeBlock() {
        return _blocks.get(Side.FRONT);
    }
}
