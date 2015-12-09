/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.world.internal;

import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;

/**
 * A single requested block change.
 *
 */
public class BlockUpdate {

    private Vector3i position;
    private Block oldType;
    private Block newType;

    /**
     * @param pos     The block position to change
     * @param newType The block type to change it to
     * @param oldType The block type to change it from
     */
    public BlockUpdate(Vector3i pos, Block newType, Block oldType) {
        this.position = pos;
        this.oldType = oldType;
        this.newType = newType;
    }

    public Vector3i getPosition() {
        return position;
    }

    public Block getOldType() {
        return oldType;
    }

    public Block getNewType() {
        return newType;
    }
}
