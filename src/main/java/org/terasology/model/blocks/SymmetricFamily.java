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

/**
 * The standard block group consisting of a single symmetrical block that doesn't need rotations
 *
 * @author Immortius <immortius@gmail.com>
 */
public class SymmetricFamily implements BlockFamily {

    Block block;

    public SymmetricFamily(Block block) {
        this.block = block;
        block.withBlockFamily(this);
    }

    public String getTitle() {
        return block.getTitle();
    }

    public byte getBlockIdFor(Side attachmentSide, Side direction) {
        return block.getId();
    }

    public Block getBlockFor(Side attachmentSide, Side direction) {
        return block;
    }

    public Block getArchetypeBlock() {
        return block;
    }
}
