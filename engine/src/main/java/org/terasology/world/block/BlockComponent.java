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
package org.terasology.world.block;

import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.Replicate;

/**
 * Used for entities representing a block in the world
 *
 */
public final class BlockComponent implements Component {
    @Replicate
    Vector3i position = new Vector3i();
    @Replicate
    Block block;

    public BlockComponent() {
    }

    /**
     * @param block The block of this component
     * @param pos The position of this component
     */
    public BlockComponent(Block block, Vector3i pos) {
        this.block = block;
        this.position.set(pos);
    }

    /**
     * @return The position of the component
     */
    public Vector3i getPosition() {
        return position;
    }

    /**
     * Set the position of the component
     * @param pos New position of the component
     */
    public void setPosition(Vector3i pos) {
        position.set(pos);
    }

    /**
     * Set block of the component
     * @param block The new block of the component
     */
    public void setBlock(Block block) {
        this.block = block;
    }

    /**
     * @return The block of the component
     */
    public Block getBlock() {
        return block;
    }
}
