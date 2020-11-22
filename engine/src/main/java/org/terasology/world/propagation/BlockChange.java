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
package org.terasology.world.propagation;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.world.block.Block;

/**
 * Represents a block change at a given position. Used to update listeners that a block has been changed
 * <p>
 * A POJO hence no methods are documented.
 */
public class BlockChange {
    private final Vector3i position = new Vector3i();
    private final Block from;
    private Block to;

    public BlockChange(Vector3ic position, Block from, Block to) {
        this.position.set(position);
        this.from = from;
        this.to = to;
    }

    public Vector3ic getPosition() {
        return position;
    }

    public Block getFrom() {
        return from;
    }

    public Block getTo() {
        return to;
    }

    public void setTo(Block block) {
        this.to = block;
    }
}
