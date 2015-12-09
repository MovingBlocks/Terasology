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

import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;

/**
 */
public class BlockChange {
    private Vector3i position;
    private Block from;
    private Block to;

    public BlockChange(Vector3i position, Block from, Block to) {
        this.position = position;
        this.from = from;
        this.to = to;
    }

    public Vector3i getPosition() {
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
